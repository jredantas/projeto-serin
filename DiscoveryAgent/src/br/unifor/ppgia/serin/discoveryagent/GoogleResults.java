package br.unifor.ppgia.serin.discoveryagent;

import java.util.List;

public class GoogleResults  {

    private ResponseData responseData;
    public ResponseData getResponseData() { return responseData; }
    public void setResponseData(ResponseData responseData) { this.responseData = responseData; }
    public String toString() { return "ResponseData[" + responseData + "]"; }

    static class ResponseData {
        private List<Result> results;
        public List<Result> getResults() { return results; }
        public void setResults(List<Result> results) { this.results = results; }
        public String toString() { return "Results[" + results + "]"; }
    }

    static class Result {
        private String url;
        private String title;
        private String visibleUrl;
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public void setUrl(String url) { this.url = url; }
        public void setTitle(String title) { this.title = title; }
		public String getVisibleUrl() { return visibleUrl; }
		public void setVisibleUrl(String visibleUrl) { this.visibleUrl = visibleUrl; }
        public Result(String url, String title, String visibleUrl){ this.url = url; this.title = title; this.visibleUrl = visibleUrl; }
        public String toString() { return "Result[url:" + url +",title:" + title + "]"; }
    }

}
