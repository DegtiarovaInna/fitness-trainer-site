package com.fitness.services.interfaces;

import java.net.URL;

public interface MediaService {
    URL generateUploadUrl(String objectKey);
    URL generateDownloadUrl(String objectKey);
}
