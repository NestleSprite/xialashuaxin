package com.example.administrator.xialashuaxin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016/11/2 0002.
 */

public class refreshlistview extends ListView implements AbsListView.OnScrollListener {
    private View mHeaderview;
    private float moveY;
    private float downY;
    private int heightt;
    private ImageView iv_arrow;
    private ProgressBar pb;
    private TextView redtext;
    private TextView timetext;
    private int paddingTop;
    private onRefreshListener refreshListener;

    private static final int PULL_REFRESH = 0;//下拉刷新
    public static final int RELEASH_REFRESH = 1;//释放刷新
    public static final int INGREFRESH = 2;//刷新中


    private int currState = PULL_REFRESH;
    private RotateAnimation rotateUpAnim;
    private RotateAnimation rotateDownAnim;
    private View mFooterview;
    private int footheightt;

    public refreshlistview(Context context) {
        super(context);
        init();
    }

    private void init() {
        initHeaderView();
        initAnimation();
        initFooterView();
        setOnScrollListener(this);
    }

    private void initFooterView() {
        mFooterview = View.inflate(getContext(), R.layout.footer, null);


        mFooterview.measure(0, 0);//按照设置的规则测量
        footheightt = mFooterview.getMeasuredHeight();//获取到测量后的高度
        mFooterview.setPadding(0, -footheightt, 0, 0);


        addFooterView(mFooterview);


    }

    private void initAnimation() {
        //向上转，箭头围绕着自身中心，逆时针旋转180


        rotateUpAnim = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateUpAnim.setDuration(300);
        rotateUpAnim.setFillAfter(true);//停在结束位置
        //向下转，箭头围绕着自身中心，逆时针旋转180
        rotateDownAnim = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateDownAnim.setDuration(300);
        rotateDownAnim.setFillAfter(true);//停在结束位置


    }

    private void initHeaderView() {
        mHeaderview = View.inflate(getContext(), R.layout.header, null);
        iv_arrow = (ImageView) mHeaderview.findViewById(R.id.iv_arrow);
        pb = (ProgressBar) mHeaderview.findViewById(R.id.pb);
        timetext = (TextView) mHeaderview.findViewById(R.id.tv_desc_last_refresh);
        redtext = (TextView) mHeaderview.findViewById(R.id.tv_title);

        mHeaderview.measure(0, 0);//按照设置的规则测量
        heightt = mHeaderview.getMeasuredHeight();//获取到测量后的高度
        mHeaderview.setPadding(0, -heightt, 0, 0);


        addHeaderView(mHeaderview);
    }

    public refreshlistview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public refreshlistview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //判断滑动距离，设置padding
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();


                break;
            //如果正在刷新中，就执行父类的处理
            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY();
                if (currState == INGREFRESH) {
                    return super.onTouchEvent(ev);
                }
                float offset = moveY - downY;
                if (offset > 0) {
                    paddingTop = (int) (offset - heightt);

                    mHeaderview.setPadding(0, paddingTop, 0, 0);
                    //改红色的字，箭头方向
                    if (paddingTop >= 0 && currState != RELEASH_REFRESH) {//完全显示
                        //切换成释放刷新模式
                        currState = RELEASH_REFRESH;
                        updateHeader();

                    } else if (paddingTop < 0 && currState != PULL_REFRESH) {
                        //切换成下拉刷新模式
                        currState = PULL_REFRESH;
                        updateHeader();

                    }
                    return true;


                }


                break;
            case MotionEvent.ACTION_UP:
                if (currState == PULL_REFRESH) {
                    mHeaderview.setPadding(0, -heightt, 0, 0);

                } else if (currState == RELEASH_REFRESH) {
                    mHeaderview.setPadding(0, 0, 0, 0);
                    currState = INGREFRESH;
                    updateHeader();

                }


                break;
            default:
                break;
        }


        return super.onTouchEvent(ev);
    }

    private void updateHeader() {
        switch (currState) {
            case RELEASH_REFRESH:
                iv_arrow.startAnimation(rotateUpAnim);
                redtext.setText("释放刷新");


                break;
            case PULL_REFRESH:
                iv_arrow.startAnimation(rotateDownAnim);
                redtext.setText("下拉刷新");


                break;
            case INGREFRESH:
                redtext.setText("刷新中...");
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(INVISIBLE);


                pb.setVisibility(VISIBLE);
                // timetext.setText("最后一次刷新时间：");
                if (refreshListener != null) {
                    refreshListener.onRefresh();//通知调用者，让其加载数据
                }


                break;
            default:
                break;

        }
    }

    public void onRefreshComplete() {
        currState = PULL_REFRESH;
        redtext.setText("下拉刷新");
        mHeaderview.setPadding(0, -heightt, 0, 0);//隐藏头布局
        pb.setVisibility(INVISIBLE);
        iv_arrow.setVisibility(VISIBLE);

        String time = getTime();


        timetext.setText("最后一次刷新时间 " + time);
    }

    private String getTime() {
        long curr = System.currentTimeMillis();
        // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(curr);


    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //如果最新状态是空闲状态，并且当前界面显示了最后一条，执行上拉加载更多
        if(scrollState==SCROLL_STATE_IDLE&&getLastVisiblePosition()>=(getCount()-1)){
            mFooterview.setPadding(0,0,0,0);
            setSelection(getCount());//使显示在footerview 最后一条
            if (refreshListener != null) {
                refreshListener.loadMore();//通知调用者，让其加载数据
            }

        }













    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


    }

    public void onLoadComplete() {
        mFooterview.setPadding(0,footheightt,0,0);
    }

    public interface onRefreshListener {
        void onRefresh();
        void loadMore();
    }

    public void setRefreshListener(onRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }
}
