package br.ufrn.dimap.dtos;

import java.time.Instant;

public record UserFileMetadata(
        String fileName, long sizeBytes, Instant lastModifiedUtc
) {

}
