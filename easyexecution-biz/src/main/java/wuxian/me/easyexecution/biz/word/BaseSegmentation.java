package wuxian.me.easyexecution.biz.word;

import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by wuxian on 16/12/2017.
 */
public abstract class BaseSegmentation implements Segmentation {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private boolean keepWhitespace = false;                 //WordConfTools.getBoolean("keep.whitespace", false);
    private boolean caseSensetive = false;                  //WordConfTools.getBoolean("keep.case", false);
    private boolean keepPunctutaion = false;                //WordConfTools.getBoolean("keep.punctuation", false);
    private int maxLength = 6;                             //WordConfTools.getInt("intercept.length", 16);

    private Dictionary dictionary = null;                   //DictionaryFactory.getDictionary();

    public BaseSegmentation() {
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary.clear();
        this.dictionary = dictionary;
    }

    /**
     * 获取词典操作接口
     * @return 词典操作接口
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    @Nullable
    public abstract List<String> segImpl(String text);

    //分词时截取的字符串的最大长度
    public int getInterceptLength() {
        if (getDictionary().getMaxLength() > maxLength) {
            return getDictionary().getMaxLength();
        }
        return maxLength;
    }

    @Override
    public List<String> seg(String text) {
        List<String> sentences = Punctuation.seg(text, keepPunctutaion); //1:先按标点进行句子分割。
        if (sentences.size() == 1) {
            return segSingleSentence(sentences.get(0));
        }

        List<String> resultList = new ArrayList<>(text.length() / 3);
        for (String sentence : sentences) {
            List<String> words = segSingleSentence(sentence);
            if (words != null && !words.isEmpty()) {
                resultList.addAll(words);
            }
        }
        sentences.clear();
        sentences = null;
        return resultList;
    }

    @Nullable
    private List<String> segSingleSentence(final String sentence) {
        if (sentence.length() == 1) {
            if (keepWhitespace) {
                List<String> result = new ArrayList<>(1);
                result.add(new String(caseSensetive ? sentence : sentence.toLowerCase()));
                return result;
            } else {
                if (!Character.isWhitespace(sentence.charAt(0))) {
                    List<String> result = new ArrayList<>(1);
                    result.add(new String(caseSensetive ? sentence : sentence.toLowerCase()));
                    return result;
                }
            }
        }

        List<String> list = segImpl(sentence);
        return list;
    }

    protected void addToCuttedList(List<String> result, String text, int start, int len) {
        String String = getWord(text, start, len);
        if (String != null) {
            result.add(String);
        }
    }

    protected void addToCuttedList(Stack<String> result, String text, int start, int len) {
        String String = getWord(text, start, len);
        if (String != null) {
            result.push(String);
        }
    }

    protected String getWord(String text, int start, int len) {
        if (len < 1) {
            return null;
        }
        if (start < 0) {
            return null;
        }
        if (text == null) {
            return null;
        }
        if (start + len > text.length()) {
            return null;
        }
        String wordText = null;
        if (caseSensetive) {
            wordText = text.substring(start, start + len);
        } else {
            wordText = text.substring(start, start + len).toLowerCase();
        }
        String String = new String(wordText);
        //方便编译器优化
        if (keepWhitespace) {
            //保留空白字符
            return String;
        } else {
            //忽略空白字符
            if (len > 1) {
                //长度大于1，不会是空白字符
                return String;
            } else {
                //长度为1，只要非空白字符
                if (!Character.isWhitespace(text.charAt(start))) {
                    //不是空白字符，保留
                    return String;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Segmentation englishSegmentation = new BaseSegmentation() {
            @Override
            public List<String> segImpl(String text) {
                List<String> words = new ArrayList<>();
                for (String String : text.split("\\s+")) {
                    words.add(new String(String));
                }
                return words;
            }
        };
        System.out.println(englishSegmentation.seg("i love programming"));
    }

}
