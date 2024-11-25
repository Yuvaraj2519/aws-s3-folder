package in.yuva.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@org.springframework.stereotype.Service
@Log4j2
public class Service {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final S3Client s3Client;

    @Autowired
    public Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String getBuckets() {
        try {
            log.info("{} :: getBuckets method :: fetching buckets", Service.class.getSimpleName());
            ListBucketsResponse buckets = s3Client.listBuckets();
            StringBuilder builder = new StringBuilder();
            buckets.buckets().forEach((bucket) -> {
                builder.append(bucket.name()).append(" created on : ").append(bucket.creationDate());
            });
            log.info("{} :: getBuckets method :: buckets fetched {}", Service.class.getSimpleName(), builder);
            return builder.toString();
        } catch (Exception e) {
            log.error("{} :: getBuckets method :: Error while fetching buckets {}", Service.class.getSimpleName(), e.getMessage());
            return "Failed to list buckets";
        }
    }

    public String uploadFileToS3(MultipartFile file) {

        String folderName = LocalDate.now().toString() + "/";

        log.info("{} :: uploadFileToS3 method :: uploading file {}",
                Service.class.getSimpleName(), file.getOriginalFilename());
        try {
            createFolderIfNotExist(folderName);
            String objectKey = folderName + file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            log.info("{} :: uploadFileToS3 method :: file uploaded :: metadata {}",
                    Service.class.getSimpleName() ,putObjectResponse.responseMetadata().toString());
            return "File "+ file.getOriginalFilename() +" uploaded successfully";
        } catch (Exception e) {
            log.error("{} :: uploadFileToS3 method :: Error while uploading file {}",
                    Service.class.getSimpleName(), e.getMessage());
            return "Failed to upload file";
        }
    }

    public void createFolderIfNotExist(String folderName) {

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();

        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

        if (!listObjectsV2Response.contents().isEmpty()) {

            log.info("{} :: createFolderIfNotExist method :: folder already exists - {}",
                    Service.class.getSimpleName(), folderName);

        } else {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.empty());

            ListObjectsV2Response listObjectsV2Response2 = s3Client.listObjectsV2(listObjectsV2Request);
            if (!listObjectsV2Response2.contents().isEmpty())
                log.info("{} :: createFolderIfNotExist method :: folder created now - {}",
                        Service.class.getSimpleName(), folderName);
            else
                log.error("{} :: createFolderIfNotExist method :: Error while creating folder {}",
                        Service.class.getSimpleName(), folderName);
        }

    }

    public String getFileFromS3(String fileName, String folderName) throws IOException {
        log.info("{} :: getFileFromS3 method :: getting file from s3 {}",
                Service.class.getSimpleName(), fileName);

        String objectKey = folderName + fileName;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            ClassPathResource classPathResource = new ClassPathResource("");
            String targetFilename = classPathResource.getPath() + fileName;
            File targetfile = new File(targetFilename);
            try (FileOutputStream output = new FileOutputStream(targetfile)){
                byte[] buffer = new byte[8 * 1024];
                int byteRead;
                while ((byteRead = response.read(buffer)) != -1){
                    output.write(buffer,0,byteRead);
                }
                log.info("{} :: getFileFromS3 method :: file downloaded successfully {}",
                Service.class.getSimpleName(), fileName);
                return "File "+fileName+" downloaded successfully";
            } catch (Exception e){
                log.error("Error while accessing download file {}", e.getMessage());
                return "Failed to download";
            }
        } catch (AwsServiceException e) {
            log.error("{} :: getFileFromS3 method :: Error getting file from s3 {}",
                    Service.class.getSimpleName(), e.getMessage());
            return "Failed to download";
        }
    }

    public String deleteFromS3(String fileName, String folderName){
        log.info("{} :: deleteFromS3 method :: deleting file from s3 {}",
                Service.class.getSimpleName(), fileName);

        String objectKey = folderName + fileName;

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        try {
            s3Client.deleteObject(deleteObjectRequest);
            log.info("{} :: deleteFromS3 method :: deleted file successfully",
                    Service.class.getSimpleName());
            return "File "+fileName+" deleted successfully";
        } catch (AwsServiceException e){
            log.error("Error while deleting file {}", e.getMessage());
            return "Failed to delete";
        }
    }
}
