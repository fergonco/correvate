package org.fergonco.zipper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Slf4j
public class ZipperController {
    private static class BadRequestException extends Exception {
        public BadRequestException(String msg) {
            super(msg);
        }
    }

    /**
     * Process the files in the body sequentially, pipe them to the zip output stream, which
     * is itself piped to the request output.
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/zip")
    public ResponseEntity<?> zip(HttpServletRequest request, HttpServletResponse response) {
        log.info("Request incoming");
        try {
            // Be sure of not closing this one before setting the response http status
            // because it closes the response OutputStream and no changes in status
            // are taken into account after that.
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                throw new BadRequestException("the message is not multipart");
            }

            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            boolean any = false;
            while (iterator.hasNext()) {
                any = true;
                FileItemStream item = iterator.next();
                if (item.isFormField()) {
                    // Fail if we get something that is not a file
                    throw new BadRequestException("form fields are not allowed");
                } else {
                    // Process the input stream
                    try (InputStream stream = item.openStream()) {
                        log.info("Zipping {}", item.getName());
                        ZipEntry entry = new ZipEntry(item.getName());
                        zos.putNextEntry(entry);
                        IOUtils.copy(stream, zos);
                        zos.closeEntry();
                        log.info("entry done");
                    }
                }
            }

            if (!any) {
                throw new BadRequestException("No file in the body");
            }

            // We cannot close the zip stream because that would close the request stream
            // Flush will not write the last part of the zip file (directory)
            // Luckily we have "finish"
            zos.finish();
            log.info("zip done");
            return ResponseEntity.ok().build();

        } catch (BadRequestException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ResponseEntity.internalServerError().body("Error processing the request");
        }
    }

}
