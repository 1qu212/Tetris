package cn.xydzjnq.tetris.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import cn.xydzjnq.tetris.R;
import cn.xydzjnq.tetris.util.SizeUtils;

public class NewRecordDialog extends Dialog {
    TextView tvScoreName;
    TextView tvScoreValue;
    TextView tvUserName;
    EditText etUserNameValue;
    View vDivider;
    TextView tvConfirm;

    private NewRecordDialog(Context context) {
        super(context);
        init();
    }

    private NewRecordDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    private NewRecordDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        setProperty(40);
        setContentView(R.layout.dialog_new_record);
        tvScoreName = findViewById(R.id.tv_score_name);
        tvScoreValue = findViewById(R.id.tv_score_value);
        tvUserName = findViewById(R.id.tv_user_name);
        etUserNameValue = findViewById(R.id.et_user_name_value);
        vDivider = findViewById(R.id.v_divider);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    public static class Builder {
        private NewRecordDialog newRecordDialog;

        public Builder(Context context) {
            newRecordDialog = new NewRecordDialog(context);
        }

        public Builder(Context context, int themeResId) {
            newRecordDialog = new NewRecordDialog(context, themeResId);
        }

        public Builder(Context context, boolean cancelable, OnCancelListener cancelListener) {
            newRecordDialog = new NewRecordDialog(context, cancelable, cancelListener);
        }

        public Builder setUserNameHint(@StringRes int userNameHint) {
            newRecordDialog.etUserNameValue.setHint(userNameHint);
            return this;
        }

        public Builder setUserNameHint(String userNameHint) {
            newRecordDialog.etUserNameValue.setHint(userNameHint);
            return this;
        }

        public Builder setScoreValue(String score) {
            newRecordDialog.tvScoreValue.setText(score);
            return this;
        }

        public NewRecordDialog builder() {
            return newRecordDialog;
        }
    }

    public String getUserNameHint() {
        return etUserNameValue.getHint().toString().trim();
    }

    public String getUserName() {
        return etUserNameValue.getText().toString().trim();
    }

    protected void setProperty(int i) {
        Window window = getWindow();
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = SizeUtils.getScreenWidth() - SizeUtils.dp2px(i);
        window.setAttributes(p);
        window.setBackgroundDrawableResource(R.drawable.shape_dialog_background);
    }
}
