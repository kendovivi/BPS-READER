package bps.android.reader.book;

public class BookInfo {
    
    public String getmPtime() {
        return mPtime;
    }

    public void setmPtime(String mPtime) {
        this.mPtime = mPtime;
    }
    private int mId;
    private String mName;
    private String mAuthor;
    private String mPublisher;
    private String mPtime;
    private String mText;
    private String mImgURLH;
    private String mImgURLV;
    
    
    public BookInfo(){
        
    }
    
    public BookInfo(int mId, String mName, String mAuthor, String mPublisher, String mPtime, String mText,
            String mImgURLH, String mImgURLV) {
        super();
        this.mId = mId;
        this.mName = mName;
        this.mAuthor = mAuthor;
        this.mPublisher = mPublisher;
        this.mPtime = mPtime;
        this.mText = mText;
        this.mImgURLH = mImgURLH;
        this.mImgURLV = mImgURLV;
    }
    
    public int getmId() {
        return mId;
    }
    public void setmId(int mId) {
        this.mId = mId;
    }
    public String getmName() {
        return mName;
    }
    public void setmName(String mName) {
        this.mName = mName;
    }
    public String getmAuthor() {
        return mAuthor;
    }
    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }
    public String getmPublisher() {
        return mPublisher;
    }
    public void setmPublisher(String mPublisher) {
        this.mPublisher = mPublisher;
    }
    public String getmText() {
        return mText;
    }
    public void setmText(String mText) {
        this.mText = mText;
    }
    public String getmImgURLH() {
        return mImgURLH;
    }
    public void setmImgURLH(String mImgURLH) {
        this.mImgURLH = mImgURLH;
    }
    public String getmImgURLV() {
        return mImgURLV;
    }
    public void setmImgURLV(String mImgURLV) {
        this.mImgURLV = mImgURLV;
    }
    
}
