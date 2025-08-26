package com.zangyalong.aicodegenerationplatform.langgraph4j.node;

import cn.hutool.ai.core.Message;
import com.zangyalong.aicodegenerationplatform.langgraph4j.ai.ImageCollectionPlanService;
import com.zangyalong.aicodegenerationplatform.langgraph4j.ai.ImageCollectionService;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageCollectionPlan;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.ImageResource;
import com.zangyalong.aicodegenerationplatform.langgraph4j.model.enums.ImageCategoryEnum;
import com.zangyalong.aicodegenerationplatform.langgraph4j.state.WorkflowContext;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.FreepikIllustrationTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.ImageSearchTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.LogoGeneratorTool;
import com.zangyalong.aicodegenerationplatform.langgraph4j.tools.MermaidDiagramTool;
import com.zangyalong.aicodegenerationplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state ->{
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：图片收集");

            // 实际执行的图片收集逻辑
            String originalPrompt = context.getOriginalPrompt();
            //String imageListStr = "";
            List<ImageResource> collectedImages = new ArrayList<>();
            try {
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 第二步：并发执行各种图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();

                // 并发执行内容图片搜索
                if (plan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }
                // 并发执行插画图片搜索
                if (plan.getIllustrationTasks() != null) {
                    FreepikIllustrationTool illustrationTool = SpringContextUtil.getBean(FreepikIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }
                // 并发执行架构图生成
                if (plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }
                // 并发执行Logo生成
                if (plan.getLogoTasks() != null) {
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }

                // 等待所有任务完成并收集结果
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                allTasks.join();
                // 收集所有结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }
                log.info("并发图片收集完成，共收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);
            log.info("图片收集完成");
            return WorkflowContext.saveContext(context);
        });
    }
}
