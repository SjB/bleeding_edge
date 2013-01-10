// Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

#ifndef EMBEDDERS_ANDROID_RESOURCE_H_
#define EMBEDDERS_ANDROID_RESOURCE_H_

#include <android_native_app_glue.h>

#include "embedders/android/log.h"

class Resource {
  public:
    Resource(android_app* application, const char* path)
        :  asset_manager_(application->activity->assetManager),
           path_(path),
           asset_(NULL),
           descriptor_(-1),
           start_(0),
           length_(-1) {
    }

    const char* path() {
      return path_;
    }

    int32_t descriptor() {
      if (Open() == 0) {
        descriptor_ = AAsset_openFileDescriptor(asset_, &start_, &length_);
        LOGI("%s has start %d, length %d, fd %d",
             path_, static_cast<int>(start_), static_cast<int>(length_),
             descriptor_);
        return descriptor_;
      }
      return -1;
    }

    off_t start() {
      return start_;
    }

    off_t length() {
      if (length_ < 0) {
        length_ = AAsset_getLength(asset_);
      }
      return length_;
    }

    int32_t Open() {
      LOGI("Attempting to open asset %s", path_);
      asset_ = AAssetManager_open(asset_manager_, path_, AASSET_MODE_UNKNOWN);
      if (asset_ != NULL) {
        return 0;
      }
      LOGE("Could not open asset %s", path_);
      return -1;
    }

    void Close() {
      if (asset_ != NULL) {
        AAsset_close(asset_);
        asset_ = NULL;
      }
    }

    int32_t Read(void* buffer, size_t count) {
      size_t actual = AAsset_read(asset_, buffer, count);
      return (actual == count) ? 0 : -1;
    }

  private:
    AAssetManager* asset_manager_;
    const char* path_;
    AAsset* asset_;
    int32_t descriptor_;
    off_t start_;
    off_t length_;
};

#endif  // EMBEDDERS_ANDROID_RESOURCE_H_