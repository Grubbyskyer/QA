package me.tianxing.service;

import me.tianxing.controller.HomeController;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TX on 2016/7/28.
 */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    //default replace word
    private static final String REPLACEMENT_WORD = "***";

    // root
    private TrieNode root = new TrieNode();

    // judge if a character needs to be filter
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    // add one word in the sensitive words list
    private void addWrod(String lineText) {
        TrieNode p = root;
        for (int i = 0; i < lineText.length(); i++) {
            Character c = lineText.charAt(i);
            if (isSymbol(c)) continue;
            if (p.getSubNode(c) == null) {
                TrieNode q = new TrieNode();
                p.setSubNode(c, q);
            }
            p = p.getSubNode(c);
        }
        p.setKeywordEnd(true);
    }

    // filter the content
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int begin = 0, end = 0;
        TrieNode p = root;
        while (end < text.length()) {
            char c = text.charAt(end);
            if (isSymbol(c)) {
                if (p == root) {
                    sb.append(c);
                    ++begin;
                }
                end++;
                continue;
            }
            p = p.getSubNode(c);
            if (p == null) {
                sb.append(text.charAt(begin));
                end = ++begin;
                p = root;
            } else if (p.isKeywordEnd()) {
                sb.append(REPLACEMENT_WORD);
                begin = ++end;
                p = root;
            } else {
                end++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // test main method
    public static void main(String[] args) {
        SensitiveService sensitiveService = new SensitiveService();
        sensitiveService.addWrod("色情");
        sensitiveService.addWrod("赌博");
        sensitiveService.addWrod("嫖娼");
        System.out.println(sensitiveService.filter("你好色情，色a情，色▄︻┳情，色情，\n不色轻"));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        root = new TrieNode();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                addWrod(line);
            }
            isr.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败！" + e.getMessage());
        }
    }

    private class TrieNode {
        // keyword end
        private boolean end = false;
        // trie node
        private Map<Character, TrieNode> map = new HashMap<Character, TrieNode>();
        // subnodes count
        public int getSubNodeCount() {
            return map.size();
        }
        //
        boolean isKeywordEnd() {
            return end;
        }
        // mark one node as the end of a keyword
        void setKeywordEnd(boolean end) {
            this.end = end;
        }
        // add a subnode
        void setSubNode(Character c, TrieNode node) {
            map.put(c, node);
        }
        // get a subnode, null if not exists
        TrieNode getSubNode(Character c) {
            return map.get(c);
        }
    }

}
