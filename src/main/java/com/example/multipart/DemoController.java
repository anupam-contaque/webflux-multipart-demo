package com.example.multipart;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class DemoController {

    @PostMapping("/transferTo")
    Flux<String> transferTo(@RequestPart("fileParts") Flux<FilePart> parts) {
        return parts.concatMap(filePart -> createTempFile(filePart.filename())
                .flatMap(tempFile -> filePart.transferTo(tempFile)
                        .then(Mono.just("File length: " + tempFile.toFile().length()))));
    }

    private Mono<Path> createTempFile(String suffix) {
        return Mono.defer(() -> {
            try {
                return Mono.just(Files.createTempFile("MultipartIntegrationTests", suffix));
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
