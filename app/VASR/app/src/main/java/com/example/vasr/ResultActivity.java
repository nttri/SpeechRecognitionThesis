package com.example.vasr;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    private EditText editText;
    private Button btnExportPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(238,238,238));

        editText = findViewById(R.id.editText);
        btnExportPDF = findViewById(R.id.btnExportPDF);

        editText.setMovementMethod(new ScrollingMovementMethod());
        editText.setBackgroundColor(Color.WHITE);

        btnExportPDF.setBackgroundColor(Color.RED);
        btnExportPDF.setTextColor(Color.WHITE);

        getSupportActionBar().setTitle("Kết quả dịch");
        String text = getIntent().getStringExtra("TEXT");
        editText.setText(text);
    }
}
