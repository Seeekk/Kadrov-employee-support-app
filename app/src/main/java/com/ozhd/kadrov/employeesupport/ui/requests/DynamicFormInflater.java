package com.ozhd.kadrov.employeesupport.ui.requests;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Динамические формы: описание полей в Room → UI без пересборки APK.
 */
public final class DynamicFormInflater {

    private DynamicFormInflater() {
    }

    public static void inflateInto(@NonNull LinearLayout container,
                                   @NonNull List<RequestFieldDefinitionEntity> fields) {
        Context ctx = container.getContext();
        container.removeAllViews();
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                ctx.getResources().getDisplayMetrics());
        for (RequestFieldDefinitionEntity f : fields) {
            TextInputLayout til = new TextInputLayout(ctx, null,
                    com.google.android.material.R.attr.textInputOutlinedStyle);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = marginPx;
            til.setLayoutParams(lp);
            til.setHint(f.label);
            if (f.inputType == FieldInputType.DATE) {
                til.setPlaceholderText(ctx.getString(R.string.field_date_hint));
            }
            TextInputEditText et = new TextInputEditText(til.getContext());
            et.setTag(f.fieldKey);
            applyInputType(ctx, et, f.inputType);
            if (f.required) {
                til.setHelperText(ctx.getString(R.string.field_required_hint));
            }
            til.addView(et);
            if (f.inputType == FieldInputType.DATE) {
                attachDatePicker(ctx, til, et);
            }
            container.addView(til);
        }
    }

    private static void applyInputType(Context ctx, TextInputEditText et, FieldInputType type) {
        switch (type) {
            case LONG_TEXT:
                et.setMinLines(3);
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case NUMBER:
                et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case DATE:
                // Hint уже задаётся на уровне TextInputLayout, чтобы не было наложения текста.
                break;
            case FILE:
                et.setFocusable(false);
                et.setClickable(true);
                et.setText(ctx.getString(R.string.field_file_stub));
                et.setInputType(InputType.TYPE_NULL);
                break;
            case TEXT:
            default:
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                break;
        }
    }

    private static void attachDatePicker(@NonNull Context ctx,
                                         @NonNull TextInputLayout til,
                                         @NonNull TextInputEditText et) {
        et.setInputType(InputType.TYPE_NULL);
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setClickable(true);
        et.setCursorVisible(false);
        View.OnClickListener open = v -> openDatePicker(ctx, et);
        et.setOnClickListener(open);
        til.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        til.setEndIconDrawable(android.R.drawable.ic_menu_my_calendar);
        til.setEndIconOnClickListener(open);
    }

    private static void openDatePicker(@NonNull Context context, @NonNull TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        CharSequence text = et.getText();
        if (text != null && text.length() >= 10) {
            try {
                String[] parts = text.toString().trim().split("-");
                if (parts.length == 3) {
                    cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                }
            } catch (NumberFormatException ignored) {
                cal = Calendar.getInstance();
            }
        }
        new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    et.setText(formatted);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @NonNull
    public static Map<String, String> collectValues(@NonNull LinearLayout fieldsContainer) {
        Map<String, String> out = new HashMap<>();
        int n = fieldsContainer.getChildCount();
        for (int i = 0; i < n; i++) {
            android.view.View row = fieldsContainer.getChildAt(i);
            if (row instanceof TextInputLayout) {
                EditText et = ((TextInputLayout) row).getEditText();
                if (et != null && et.getTag() instanceof String) {
                    String key = (String) et.getTag();
                    CharSequence text = et.getText();
                    out.put(key, text != null ? text.toString().trim() : "");
                }
            }
        }
        return out;
    }
}
