package com.qinshou.passwordedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:密码输入框,也可明文显示,用作输入验证码
 * Created by 禽兽先生
 * Created on 2017/8/17
 */

public class PasswordEditText extends RelativeLayout {
    private Context mContext;
    private EditText mEditText;
    private List<EditText> editTexts;   //选用 EditText 而不用 TextView 是因为 EditText 可以密文显示
    private int count = 4;  //密码框数量
    private int passwordSize = 24;  //密码文本大小
    private boolean showPassword = true;   //密码是否密文显示,true 为一直明文显示,false 为 0.5s 后密文显示
    private int bgInputing = R.drawable.bg_inputing; //待输入的密码框的背景
    private int bgInputed = R.drawable.bg_inputed;  //非待输入的密码框的背景
    private onCompletionListener mOnCompletionListener;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttr(attrs, defStyleAttr);
        initView();
        addListener();
    }

    /**
     * Description:初始化自定义属性
     * Date:2017/8/19
     */
    private void initAttr(AttributeSet attrs, int defStyleAttr) {
        TypedArray mTypeArray = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordEditText, defStyleAttr, 0);
        count = mTypeArray.getInt(R.styleable.PasswordEditText_count, 4);
        passwordSize = mTypeArray.getInt(R.styleable.PasswordEditText_passwordSize, 24);
        showPassword = mTypeArray.getBoolean(R.styleable.PasswordEditText_showPassword, true);
        bgInputing = mTypeArray.getResourceId(R.styleable.PasswordEditText_bgInputing, R.drawable.bg_inputing);
        bgInputed = mTypeArray.getResourceId(R.styleable.PasswordEditText_bgInputed, R.drawable.bg_inputed);
    }

    /**
     * Description:添加控件,密码框和不可见的输入框
     * Date:2017/8/18
     */
    private void initView() {
        //新建一个容器
        LinearLayout mLinearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(linearLayoutParams);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //根据 count 添加一定数量的密码框
        editTexts = new ArrayList<EditText>();
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(getScreenWidth(mContext) / count - dip2px(mContext, 20), getScreenWidth(mContext) / count - dip2px(mContext, 20));
        textViewParams.setMargins(dip2px(mContext, 10), dip2px(mContext, 10), dip2px(mContext, 10), dip2px(mContext, 10));
        for (int i = 0; i < count; i++) {
            EditText mEditText = new EditText(mContext);
            mEditText.setLayoutParams(textViewParams);
            mEditText.setBackgroundResource(R.drawable.bg_inputed); //设置背景
            mEditText.setGravity(Gravity.CENTER);   //设置文本显示位置
            mEditText.setTextSize(passwordSize);    //设置文本大小
            mEditText.setFocusable(false);  //设置无法获得焦点
            editTexts.add(mEditText);
            mLinearLayout.addView(mEditText);
        }
        editTexts.get(0).setBackgroundResource(bgInputing);

        //添加不可见的 EditText
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mEditText = new EditText(mContext);
        mEditText.setLayoutParams(editTextParams);
        mEditText.setCursorVisible(false);  //设置输入游标不可见
        mEditText.setBackgroundResource(0); //设置透明背景,让下划线不可见
        mEditText.setAlpha(0.0f);   //设置为全透明,让输入的内容不可见
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);    //设置只能输入数字
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(count)});   //限制输入长度

        addView(mLinearLayout);
        addView(mEditText);
    }

    /**
     * Description:为输入框添加监听器
     * Date:2017/8/18
     */
    private void addListener() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             *
             * @param s 输入后的文本
             * @param start 输入的位置
             * @param before 输入的位置上之前的字符数
             * @param count 输入的位置上新输入的字符数
             */
            @Override
            public void onTextChanged(CharSequence s, final int start, int before, int count) {
                if (before == 0 && count == 1) {
                    //为对应显示框设置对应显示内容
                    editTexts.get(start).setText(s.subSequence(start, start + 1));
                    //修改输入了内容的密码框的背景
                    editTexts.get(start).setBackgroundResource(bgInputed);
                    //如果还有下一个密码框,将其背景设置为待输入的背景
                    if (start + 1 < editTexts.size()) {
                        editTexts.get(start + 1).setBackgroundResource(bgInputing);
                    } else {
                        //输入完成后关闭软键盘
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        //如果添加了监听器,则回调
                        if (mOnCompletionListener != null) {
                            mOnCompletionListener.onCompletion(s.toString());
                        }
                    }
                    //如果需要密文显示,则 0.5s 后设置为密文显示
                    if (!showPassword) {
                        editTexts.get(start).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editTexts.get(start).setTransformationMethod(PasswordTransformationMethod.getInstance());
                            }
                        }, 500);
                    }
                    //如果上一个显示框还不是密文显示的话,立即将其设置为密文显示,前提是需要密文显示
                    if (!showPassword && start > 0 && editTexts.get(start - 1).getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                        editTexts.get(start - 1).setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                } else if (before == 1 && count == 0) {
                    //清除退格位置对应显示框的内容
                    editTexts.get(start).setText("");
                    //将其退格的位置设置为明文显示
                    editTexts.get(start).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //设置退格位置的背景
                    for (EditText editText : editTexts) {
                        editText.setBackgroundResource(bgInputed);
                    }
                    editTexts.get(start).setBackgroundResource(bgInputing);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setOnCompleteListener(onCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    /**
     * Description:获取屏幕宽度
     * Date:2017/8/18
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public interface onCompletionListener {
        void onCompletion(String code);
    }
}

