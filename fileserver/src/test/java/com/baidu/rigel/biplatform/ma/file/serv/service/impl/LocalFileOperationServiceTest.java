/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.ma.file.serv.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import com.baidu.rigel.biplatform.ma.file.serv.service.FileLocation;
import com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils;

/**
 * 
 * test class
 * @author david.wang
 * @version 1.0.0.1
 */
//@RunWith(PowerMockRunner.class)
public class LocalFileOperationServiceTest {
    
    /**
     * 系统临时目录
     */
    private String dir = System.getProperty("user.dir");
    
    /**
     * 文件服务
     */
    private LocalFileOperationServiceImpl service = new LocalFileOperationServiceImpl(
            new FileLocation(dir));
    
    /**
     * 
     */
    @Test
    public void testGetFileAttributeWithEmptyPath() {
        Map<String, Object> rs = service.getFileAttributes(null);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件名称为空", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testGetFileAttributesWithUnexistFile() {
        new File (dir + "/test.txt").delete ();
        Map<String, Object> rs = service.getFileAttributes(dir + "/test.txt");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件不存在", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testFileIsDir() {
        File file = PowerMockito.mock(File.class);
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(file.isDirectory()).thenReturn(true);
        Map<String, Object> rs = service.getFileAttributes(dir);
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("directory", rs.get("type"));
    }
    
    /**
     * 
     */
    @Test
    public void testGetFileAttribute() {
        File file = PowerMockito.mock(File.class);
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(file.isDirectory()).thenReturn(true);
        Map<String, Object> rs = service.getFileAttributes(dir);
        Assert.assertNotNull(rs);
    }
    
    /**
     * 
     */
    @Test
    public void testRmWithEmptyName() {
        Map<String, Object> rs = service.rm(null);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件路径为空", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testRmUnexistFile() {
        new File (dir + "/test.txt").delete ();
        Map<String, Object> rs = service.rm(dir + "/test.txt");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("删除文件不存在", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void tetRmFailed() {
        File file = PowerMockito.mock(File.class);
        Mockito.when(file.exists()).thenReturn(false);
        Map<String, Object> rs = service.rm(dir + "/");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件删除失败", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testRm() {
        File file = new  File (dir + "/test.txt");
        if (!file.exists ()) {
            try {
                file.createNewFile ();
            } catch (IOException e) {
                Assert.fail ();
            }
        }
        Map<String, Object> rs = service.rm(dir + "/test.txt");
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("文件删除成功", rs.get("msg"));
        Assert.assertFalse (new File (dir + "/test.txt").exists ());
    }
    
    /**
     * 
     */
    @Test
    public void testMkdirWithEmptyPath() {
        Map<String, Object> rs = service.mkdir(null);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("目标地址为空", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void tetMkdirWithInvalidePath() {
        Map<String, Object> rs = service.mkdir(".");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("目标地址不能为[.]或者[..]", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void tetMkdirWithParentPath() {
        Map<String, Object> rs = service.mkdir("..");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("目标地址不能为[.]或者[..]", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void tetMkdirWithExistPath() {
        Map<String, Object> rs = service.mkdir(dir);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("目录已经存在", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testMkdir() {
        File file = new File (dir + "/test");
        file.delete ();
        Map<String, Object> rs = service.mkdir(dir + "/test");
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("创建目录成功", rs.get("msg"));
        new File(dir + "/test").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testMkdirFailed() {
        new File (dir + "/test/test").delete ();
        new File (dir + "/test").delete ();
        Map<String, Object> rs = service.mkdir(dir + "/test/test");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("创建目录失败", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testMkdirs() {
        new File (dir + "/test/test").delete ();
        new File (dir + "/test").delete ();
        Map<String, Object> rs = service.mkdirs(dir + "/test/test");
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("创建目录成功", rs.get("msg"));
        new File (dir + "/test/test").delete ();
        new File (dir + "/test").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testWriteWithEmptyPath() {
        Map<String, Object> rs = service.write(null, null, true);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件路径为null", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testWriteWithEmptyContent() {
        new File (dir + "/test.txt").delete ();
        Map<String, Object> rs = service.write(dir + "/test.txt", null, true);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("文件内容为空", rs.get("msg"));
        new File(dir + "/test.txt").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testWriteWithNotOverride() {
        new File (dir + "/test.txt").delete ();
        try {
            new File (dir + "/test.txt").createNewFile ();
        } catch (IOException e) {
            Assert.fail ();
        }
        Map<String, Object> rs = service.write(dir, new byte[10], false);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("该文件已经存在", rs.get("msg"));
        new File (dir + "/test.txt").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testWrite() {
        new File (dir + "/test.txt").delete ();
        Map<String, Object> rs = service.write(dir + "/test.txt", new byte[10], false);
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("新文件写入成功", rs.get("msg"));
        new File (dir + "/test.txt").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testLsWithEmptyPath() {
        Map<String, Object> rs = service.ls(null);
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("请求路径为空", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testLsWithNotExistsPath() {
        new File (dir + "/test/test").delete ();
        new File (dir + "/test").delete ();
        Map<String, Object> rs = service.ls(dir + "/test/test");
        Assert.assertNotNull(rs);
        Assert.assertEquals("fail", rs.get("result"));
        Assert.assertEquals("请求路径不存在", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testLs() {
        Map<String, Object> rs = service.ls(dir);
        Assert.assertNotNull(rs);
        Assert.assertEquals("success", rs.get("result"));
        Assert.assertEquals("请求文件列表成功", rs.get("msg"));
    }
    
    /**
     * 
     */
    @Test
    public void testMv() {
        new File (dir + "/test.txt").delete ();
        Map<String, Object> rs = LocalFileOperationUtils.mv(dir + "/test.txt", "/test_mv.txt", true);
        Assert.assertNotNull(rs);
        Assert.assertFalse (new File (dir + "/test.txt").exists ());
        new File (dir + "/test_mv.txt").delete ();
    }
    
    /**
     * 
     */
    @Test
    public void testCopy() {
        new File (dir + "/test.txt").delete ();
        try {
            new File (dir + "/test.txt").createNewFile ();
        } catch (IOException e) {
            Assert.fail ();
        }
        Map<String, Object> rs = LocalFileOperationUtils.copy(dir + "/test.txt", "/test_cp.txt", true);
        Assert.assertNotNull(rs);
        new File (dir + "/test.txt").delete ();
    }
}
