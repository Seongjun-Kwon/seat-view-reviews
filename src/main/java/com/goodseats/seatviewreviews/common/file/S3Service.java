package com.goodseats.seatviewreviews.common.file;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3Service implements FileStorageService {

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket-name}")
	private String bucketName;

	@Override
	public String upload(MultipartFile multipartFile, String subPath) {
		if (multipartFile.isEmpty()) {
			throw new IllegalArgumentException(BAD_LOGIN_REQUEST.getMessage());
		}

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(multipartFile.getContentType());
		String savedFilename = createSavedFilename(multipartFile.getOriginalFilename());
		String fileKey = subPath + "/" + savedFilename;

		try {
			amazonS3.putObject(
					bucketName, fileKey, multipartFile.getInputStream(), objectMetadata
			);
		} catch (Exception e) {
			throw new AmazonS3Exception(BAD_IMAGE_REQUEST.getMessage(), e);
		}

		return String.valueOf(amazonS3.getUrl(bucketName, fileKey));
	}

	@Override
	public void delete(String subpath, String savedFilename) {
		String fileKey = subpath + "/" + savedFilename;

		try {
			amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
		} catch (Exception e) {
			throw new AmazonS3Exception(NOT_FOUND.getMessage(), e);
		}
	}

	private String createSavedFilename(String originalFilename) {
		String uuid = UUID.randomUUID().toString();
		return uuid + "." + extractExtension(originalFilename);
	}

	private String extractExtension(String originalFilename) {
		int beforeExtensionIndex = originalFilename.lastIndexOf(".");
		return originalFilename.substring(beforeExtensionIndex + 1);
	}
}
