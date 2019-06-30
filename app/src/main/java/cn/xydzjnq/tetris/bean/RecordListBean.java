package cn.xydzjnq.tetris.bean;

import java.util.List;

public class RecordListBean {
    private List<RecordBean> recordBeanList;

    public List<RecordBean> getRecordBeanList() {
        return recordBeanList;
    }

    public void setRecordBeanList(List<RecordBean> recordBeanList) {
        this.recordBeanList = recordBeanList;
    }

    public static class RecordBean {
        private String name;
        private String score;
        private String time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
