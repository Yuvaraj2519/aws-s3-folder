package in.yuva.aws;

import java.time.LocalDate;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@org.springframework.stereotype.Service
@Log4j2
public class Service {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final S3Client s3Client;

    public Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String getBuckets() {
        try {
            ListBucketsResponse buckets = s3Client.listBuckets();
            StringBuilder builder = new StringBuilder();
            buckets.buckets().forEach((bucket) -> {
                builder.append(bucket.name()).append(" created on :").append(bucket.creationDate());
            });
            log.info("{} :: getBuckets method :: fetched buckets {}", Service.class.getSimpleName(), builder);
            return builder.toString();
        } catch (Exception var3) {
            Exception e = var3;
            log.error("{} :: getBuckets method :: Error while fetching buckets {}", Service.class.getSimpleName(), e.getMessage());
            return "Failed to list buckets";
        }
    }

    public String upload() {
        String folderName = LocalDate.now().toString() + "/";
        createFolderIfNotExist(folderName);
        return folderName + " created";
    }

    public void createFolderIfNotExist(String folderName) {

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();

        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

        if (!listObjectsV2Response.contents().isEmpty()) {

            log.info("{} :: createFolderIfNotExist method :: folder already exists - {}",
                    Service.class.getName(), folderName);

        } else {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.empty());
            log.info("{} :: createFolderIfNotExist method :: folder created now - {}",
                    Service.class.getName(), folderName);

        }

    }
}
