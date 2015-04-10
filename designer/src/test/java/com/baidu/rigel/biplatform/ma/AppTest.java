
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
package com.baidu.rigel.biplatform.ma;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/** 
 *  
 * @author xiaoming.chen
 * @version  2015年2月12日 
 * @since jdk 1.8 or after
 */
public class AppTest {
    
    @Test
    public void testQuery() throws IOException {
        Assert.assertEquals (1, 1);
//        File f = new File("c:/QuestionModel");
//        BufferedReader br = new BufferedReader(new FileReader(f));
//        StringBuilder sb = new StringBuilder();
//        String lineStr = null;
//        while ((lineStr = br.readLine()) != null) {
//            sb.append(lineStr);
//            sb.append(System.getProperty("line.separator"));
//        }
//        System.out.println(sb.toString());
//        ConfigQuestionModel question = AnswerCoreConstant.GSON.fromJson(sb.toString(), ConfigQuestionModel.class);
//        System.out.println(question);
//
//        br.close();
//        //
//        JsonUnSeriallizableUtils.fillCubeInfo(question.getCube());
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("question", sb.toString());
//        //
//        // // System.out.println(question.getCube().lookUp(question.getDataSourceInfo(), "[trade].[All_trades]",
//        // question.getRequestParams()));
//        //
//        //
//        //
//        List<Cube> cubes = new ArrayList<Cube>();
//        cubes.add(question.getCube());
//        // MiniCubeConnection connection = MiniCubeDriverManager.getConnection(question.getDataSourceInfo());
//        // connection.publishCubes(cubes, question.getDataSourceInfo());
//        //
//        //
//        //
//        // // Dimension tradeDim = question.getCube().getDimensions().get("dim_pos");
//        // // Level tLevel = tradeDim.getLevels().get("shw");
//        // //
//        // // Member allMember = tradeDim.getAllMember();
//        // //
//        // // List<Member> members = allMember.getChildMembers(question.getCube(), question.getDataSourceInfo(), null);
//        // // System.out.println(members);
//        // //
//        // // List<Member> children = members.get(0).getChildMembers(question.getCube(), question.getDataSourceInfo(),
//        // null);
//        // //
//        // // System.out.println(tLevel.getMembers(question.getCube(), question.getDataSourceInfo(), null));
//        //
//        //
//        //
//        String result = HttpRequest.sendPost("http://127.0.0.1:8080/query", params);
//        System.out.println(result);
//        ResponseResult responseResu = AnswerCoreConstant.GSON.fromJson(result, ResponseResult.class);
//        System.out.println(responseResu.getData());
//        System.out.println(responseResu.getData().replace("\\", ""));
//        String dataModelJson = responseResu.getData().replace("\\", "");
//        dataModelJson = dataModelJson.substring(1, dataModelJson.length() - 1);
//        System.out.println("++" + dataModelJson);
//        
//        MiniCubeSqlConnection connection = (MiniCubeSqlConnection) MiniCubeDriverManager.getConnection(question.getDataSourceInfo());
//        DataModel dm = connection.query(question);
//        
//        PivotTable pTable = DataModelUtils.transDataModel2PivotTable(question.getCube(), dm, false, 500, true);
//        
//        System.out.println(AnswerCoreConstant.GSON.toJson(pTable));
        
    }

}

