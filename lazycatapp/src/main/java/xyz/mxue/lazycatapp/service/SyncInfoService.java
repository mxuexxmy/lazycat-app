package xyz.mxue.lazycatapp.service;


import xyz.mxue.lazycatapp.model.vo.SyncDataSourceVO;
import xyz.mxue.lazycatapp.model.vo.SyncInfoVO;

import java.util.List;

public interface SyncInfoService {
    List<SyncInfoVO> syncInfo();

    List<SyncDataSourceVO> syncDataSource();
}
