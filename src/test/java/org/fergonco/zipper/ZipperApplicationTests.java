package org.fergonco.zipper;

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ZipperApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private FileSystemResource makeFile(String name) throws IOException {
        byte[] content = "hola mundo".getBytes(StandardCharsets.UTF_8);
        Path path = new File("/tmp", name).toPath();
        Files.write(path, content);
        return new FileSystemResource(path);
    }
    @Test
    void testTwoFiles() throws IOException {
        FileSystemResource[] files  = new FileSystemResource[]{makeFile("a"), makeFile("b")};
        ResponseEntity<byte[]> ret = callEndpoint(files);
        System.out.println(ret.getStatusCode());
    }

    private ResponseEntity<byte[]> callEndpoint(FileSystemResource[] files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (FileSystemResource file : files) {
            body.add("files", file);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<byte[]> ret = this.restTemplate
                .postForEntity("http://localhost:" + port + "/zip", new HttpEntity<>(body, headers), byte[].class);
        Assertions.assertEquals(200, ret.getStatusCodeValue());
        return ret;
    }

}
