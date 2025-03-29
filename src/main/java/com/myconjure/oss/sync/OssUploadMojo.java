package com.myconjure.oss.sync;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Maven 插件：在 package 阶段后自动上传 JAR 到阿里云 OSS
 */
@Mojo(name = "upload", defaultPhase = LifecyclePhase.PACKAGE)
public class OssUploadMojo extends AbstractMojo {

    /**
     * 阿里云 OSS 访问密钥 ID
     */
    @Parameter(property = "oss.accessKeyId", required = true)
    private String accessKeyId;

    /**
     * 阿里云 OSS 访问密钥 Secret
     */
    @Parameter(property = "oss.accessKeySecret", required = true)
    private String accessKeySecret;

    /**
     * OSS 服务器 Endpoint
     */
    @Parameter(property = "oss.endpoint", required = true)
    private String endpoint;

    /**
     * OSS 存储桶名称
     */
    @Parameter(property = "oss.bucketName", required = true)
    private String bucketName;

    /**
     * 上传路径，例如 "maven-releases/"
     */
    @Parameter(property = "oss.targetPath", defaultValue = "")
    private String targetPath;

    /**
     * Maven 项目构建的 JAR 文件
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar", readonly = true)
    private File jarFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!jarFile.exists()) {
            throw new MojoExecutionException("JAR 文件未找到: " + jarFile.getAbsolutePath());
        }

        getLog().info("上传 JAR 到阿里云 OSS...");
        getLog().info("JAR 文件: " + jarFile.getAbsolutePath());

        // 生成目标 OSS Key
        String ossKey = targetPath + jarFile.getName();

        // 上传文件
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, ossKey, jarFile);
            getLog().info("上传成功: " + ossKey);
        } catch (Exception e) {
            throw new MojoExecutionException("OSS 上传失败", e);
        } finally {
            ossClient.shutdown();
        }
    }
}