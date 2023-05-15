package com.goodseats.seatviewreviews.common.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	String upload(MultipartFile multipartFile, String subPath);

	void delete(String subpath, String savedFilename);
}
