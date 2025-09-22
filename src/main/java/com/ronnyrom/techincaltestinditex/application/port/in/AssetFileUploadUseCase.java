package com.ronnyrom.techincaltestinditex.application.port.in;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;

public interface AssetFileUploadUseCase {
    AssetDomain uploadAssetFile(AssetDomain assetDomain);
}
