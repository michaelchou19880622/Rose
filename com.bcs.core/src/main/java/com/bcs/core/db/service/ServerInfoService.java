//package com.bcs.core.db.service;
//
//import com.bcs.core.db.entity.ServerInfo;
//import com.bcs.core.db.repository.ServerInfoRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * @author Alan
// */
//@Service
//public class ServerInfoService {
//
//    private ServerInfoRepository serverInfoRepository;
//
//    @Autowired
//    public ServerInfoService(ServerInfoRepository serverInfoRepository) {
//        this.serverInfoRepository = serverInfoRepository;
//    }
//
//    public void saveServerInfo(ServerInfo serverInfo) {
//        serverInfoRepository.save(serverInfo);
//    }
//
//    public List<ServerInfo> findServerInfoByType(ServerInfo.ServerType type) {
//        return serverInfoRepository.findByType(type);
//    }
//
//    public void updateServerInfo() {
//
//    }
//
//    public void deleteServerInfo() {
//
//    }
//}
