/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.filesystem.azure;

import com.azure.core.exception.AzureException;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import io.trino.filesystem.TrinoFileSystemException;

import java.io.FileNotFoundException;
import java.io.IOException;

final class AzureUtils
{
    private AzureUtils() {}

    public static IOException handleAzureException(RuntimeException exception, String action, AzureLocation location)
            throws IOException
    {
        if (isFileNotFoundException(exception)) {
            throw withCause(new FileNotFoundException(location.toString()), exception);
        }
        if (exception instanceof AzureException) {
            throw new TrinoFileSystemException("Azure service error %s file: %s".formatted(action, location), exception);
        }
        throw new IOException("Error %s file: %s".formatted(action, location), exception);
    }

    public static boolean isFileNotFoundException(RuntimeException exception)
    {
        if (exception instanceof BlobStorageException blobStorageException) {
            return BlobErrorCode.BLOB_NOT_FOUND.equals(blobStorageException.getErrorCode());
        }
        if (exception instanceof DataLakeStorageException dataLakeStorageException) {
            return "PathNotFound" .equals(dataLakeStorageException.getErrorCode());
        }
        return false;
    }

    private static <T extends Throwable> T withCause(T throwable, Throwable cause)
    {
        throwable.initCause(cause);
        return throwable;
    }
}
