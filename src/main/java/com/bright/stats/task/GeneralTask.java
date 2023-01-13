package com.bright.stats.task;

import com.bright.stats.pojo.po.primary.OperationLog;
import com.bright.stats.repository.primary.OperationLogRepository;
import com.bright.stats.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: Tz
 * @Date: 2022/10/31 18:15
 */
@Configuration
@EnableScheduling
@PropertySource(value = "classpath:/config/ScheulingTaskTime.properties", encoding = "UTF-8")
public class GeneralTask {

    @Resource
    private OperationLogRepository operationLogRepository;

    @SneakyThrows
    @Transactional(rollbackFor = Throwable.class)
    @Scheduled(cron = "${OperationSaveToFileTaskTime}")
    public void operationLogSaveToFileTask(){
        //执行备份操作日志文件

        //保存操作日志的文件夹名
        String operationFolderName = "operation_log_bak";

        //保存操作日志的文件夹名
        String operationFileName = "operation.bak";
        String userPath = System.getProperty("user.dir");
        String cureTime = DateUtil.getDate(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        String lastYear = DateUtil.getDate(calendar.getTime(), 1);
        String lastMonths = DateUtil.getDate(calendar.getTime(), 2);
        String lastDays = DateUtil.getDate(calendar.getTime(), 3);
        String lastTime = lastYear + "-" + lastMonths + "-" + lastDays;

        operationFileName = lastTime + "_" + operationFileName;


        //备份操作日志的路径
        String saveFolderName = userPath + File.separator + operationFolderName + File.separator + cureTime;
        File saveFileFolderObject = new File(saveFolderName);

        if(!saveFileFolderObject.exists()){
            saveFileFolderObject.mkdirs();
        }

        String saveFileName = saveFolderName + File.separator + operationFileName;
        File saveFileObject = new File(saveFileName);
        if(!saveFileObject.exists()){
            saveFileObject.createNewFile();
        }


        List<OperationLog> operationLogList = operationLogRepository
                .findByBetweenOpeBeginDate(DateUtil.convert(lastTime), DateUtil.convert(cureTime));


        if(CollectionUtils.isEmpty(operationLogList)){
            return;
        }

        //将数据写入文件
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(saveFileName));
            for (int j = 0; j < operationLogList.size(); j++) {
                bw.append(operationLogList.get(j).toString());
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            saveFileObject.delete();
            e.printStackTrace();
        } finally {
            if(bw != null){
                bw.close();
            }
        }

        try{
            //删除写入的数据
            operationLogRepository.deleteAll(operationLogList);
        } catch (Exception e){
            saveFileObject.delete();
        }

    }

}
