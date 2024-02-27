package org.example.service;

import org.example.entity.AiAnalysisLog;
import org.example.repository.AiAnalysisLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AiAnalysisLogService {

    @Autowired
    private AiAnalysisLogRepository aiAnalysisLogRepository;

    @Transactional
    public void saveData(AiAnalysisLog aiAnalysisLog) {
        //保存
        aiAnalysisLogRepository.save(aiAnalysisLog);
    }
}
