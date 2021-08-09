package com.athanstyx.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }


    }

    //将一个敏感词添加到前缀树当中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            //将指针指向子节点, 进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }


    /**
     * 过滤敏感词
     *
     * @param text 待过滤文本
     * @return 已经过滤完成的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                /**
                 * 分为2种情况,
                 * 第一种是指针1处于根节点, 所以这个符号不在怀疑的范围内, 所以将该字符纳入结果中, 并将指针2,3都往下移一位
                 * 第二种是指针1不处于根节点, 所以这个符号在怀疑的范围内, 先不将该字符纳入结果中, 并且将指针3往下移一位
                 */
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检测下级节点, 查看这个字符是否存在于该前缀树的子节点中, 通过查看该节点的hashmap中是否有c这个key 的value
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) { // 没有, 说明这个以begin开头的字符串不在敏感词的范围中,所以该字符不可能为敏感字符,所以将begin加入结果中, 并再从这个字符串的第二个位置开始判断
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) { // 说明该字符串是一个敏感词, 将该字符串替换为固定的字符串, 并且进入到下一个位置上
                //发现敏感词, 将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            } else {    //说明这个字符是在敏感范围的, 所以这个字符串还存在风险, 所以继续下一个位置的判定.
                //检测下一个字符
                position++;
            }
        }

        //将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();

    }

    //判断是否为符号
    private boolean isSymbol(Character character) {
        //0x2E80~0x9FFF是东亚文件范围
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }

    //前缀树

    /**
     * 这里的前缀树是通过每一层都用一个hashmap来表示, 其中key就是这一层的字符, 而value就是这个字符对应的节点
     * 构建前缀树:
     *    所以对于每一个敏感字符串,我们都通过先查询节点中的map属性中是否有含有该字符的value(也就是节点), 如果没有就新建一个节点,并将该
     * 节点添加到这个字符对应的value中, 然后再移动到下一个字符, 再进行上面的判断
     * 查询是否是敏感词:
     *    对于要查询是否是敏感词的c, 我们首先先对前缀树的下一层map中查询是否有该字符对应的value(节点)(因为前缀树的根节点是空的,所以
     * 我们每次都是查询的下一层的hashmap), 如果有该字符对应的节点那么就将c字符移动的到下一个字符, 并且再通过属性获取这个节点的下一子层
     * 的map
     *
     * 更正版本:
     * 这里的前缀树是通过每个节点都有一个hashmap属性, 这个hashmap属性就是用来表示这个字节的子节点集合, 每个子节点的值和节点对象构成了key
     * 和value 来表示子节点集合
     */
    private class TrieNode{
        //关键词结束的标识
        private boolean isKeywordEnd = false;

        /**
         * 子节点
         * key:下级字符
         * value:下级节点
         */
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }


}
