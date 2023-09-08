package com.goodseats.seatviewreviews.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	String upload(MultipartFile multipartFile, String subPath);

	void delete(String subpath, String savedFilename);
}
