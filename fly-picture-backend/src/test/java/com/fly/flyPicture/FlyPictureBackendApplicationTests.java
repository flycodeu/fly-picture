package com.fly.flyPicture;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class FlyPictureBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        /**
         * tn=resultjson_com：必带的参数
         * word：搜索关键词
         * pn：分页数，传入30的倍数，第一次为30，第二次为60，以此内推
         */
        String searchText = "旅游";
        String url = String.format("https://images.baidu.com/search/acjson?tn=resultjson_com&word=%s&pn=%s", searchText, 30);
        String resData = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(resData);
        JSONArray list = jsonObject.getJSONArray("data");
        for (int i = 0; i < list.size(); i++) {
            JSONObject res = list.get(i, JSONObject.class);
            String thumbURL = res.getStr("thumbURL");
            System.out.println(i + "=======" + thumbURL);
        }

    }

}
