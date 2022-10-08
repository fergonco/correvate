package org.fergonco.zipper;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.xml.stream.events.StartDocument;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ZipperApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Makes a file in /tmp with the specified name and contents.
     *
     * @param name
     * @param textContent
     * @return
     * @throws IOException
     */
    private FileSystemResource makeFile(String name, String textContent) throws IOException {
        byte[] bytes = textContent.getBytes(StandardCharsets.UTF_8);
        Path path = new File("/tmp", name).toPath();
        Files.write(path, bytes);
        return new FileSystemResource(path);
    }

    /**
     * Test that we can send a couple of files and we get a zip file with the proper contents
     *
     * @throws IOException
     */
    @Test
    void testTwoFiles() throws IOException {
        String contentsA = "sadgjsgd";
        String contentsB = "19351nsa;;3";
        FileSystemResource[] files = new FileSystemResource[]{makeFile("a", contentsA), makeFile("b", contentsB)};
        ResponseEntity<byte[]> ret = callEndpoint(files);
        Map<String, String> fileContents = unzip(ret.getBody());
        Assertions.assertEquals(contentsA, fileContents.get("a"));
        Assertions.assertEquals(contentsB, fileContents.get("b"));
    }

    /**
     * If we pass no files in the body we want to get a 400
     */
    @Test
    void testNoFiles() {
        FileSystemResource[] files = new FileSystemResource[0];
        ResponseEntity<byte[]> ret = callEndpoint(files);
        Assertions.assertEquals(400, ret.getStatusCodeValue());
    }

    /**
     * If we have a parameter in the form-data that is not a file
     * upload we want to return 400
     */
    @Test
    void testMultipartParam() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", "value");
        ResponseEntity<byte[]> ret = callEndpoint(body);
        Assertions.assertEquals(400, ret.getStatusCodeValue());
    }

    /**
     * Unzips the contents passed as parameter and returns a map from the file names
     * to the file contents.
     *
     * @param body
     * @return
     * @throws IOException
     */
    private Map<String, String> unzip(byte[] body) throws IOException {
        Map<String, String> ret = new HashMap<>();
        ZipInputStream unzip = new ZipInputStream(new ByteArrayInputStream(body));
        ZipEntry entry;
        while ((entry = unzip.getNextEntry()) != null) {
            String content = IOUtils.toString(unzip, StandardCharsets.UTF_8);
            ret.put(entry.getName(), content);
            unzip.closeEntry();
        }
        unzip.close();
        return ret;
    }

    /**
     * Calls the zip endpoint as multipart form-data uploading the specified files
     *
     * @param files
     * @return
     */
    private ResponseEntity<byte[]> callEndpoint(FileSystemResource[] files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (FileSystemResource file : files) {
            body.add("files", file);
        }
        return callEndpoint(body);
    }

    /**
     * Calls the zip endpoint as multipart form-data with the specific body
     *
     * @param body
     * @return
     */
    private ResponseEntity<byte[]> callEndpoint(MultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<byte[]> ret = this.restTemplate
                .postForEntity("http://localhost:" + port + "/zip", new HttpEntity<>(body, headers), byte[].class);
        return ret;
    }

}
