package com.example.vasr;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private EditText editText;
    private Button btnExportPDF;
    private String resultText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        editText = findViewById(R.id.editText);
        btnExportPDF = findViewById(R.id.btnExportPDF);

        setupUI();
    }

    private void setupUI() {
        //set title on toolbar
        getSupportActionBar().setTitle("Kết quả dịch");

        //set activity background color
        getWindow().getDecorView().setBackgroundColor(Color.rgb(238,238,238));

        //add back button on toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setup edittext
        editText.setMovementMethod(new ScrollingMovementMethod());
        editText.setBackgroundColor(Color.WHITE);
        resultText = getIntent().getStringExtra("TEXT");
        editText.setText(resultText);

        //setup export PDF button
        btnExportPDF.setBackgroundColor(Color.rgb(0,150,136));
        btnExportPDF.setTextColor(Color.WHITE);
        btnExportPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                exportPDF();
                exportTXT();
            }
        });
    }

//    private void exportPDF() {
//        //get text
//        String text = editText.getText().toString();
//        Document document = new Document();
//        String dateString = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
//        String fileName = "VASR_" + dateString + ".pdf";
//        String filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;
//
//        try {
//            PdfWriter.getInstance(document, new FileOutputStream(filePath));
//            document.open();
//            document.add(new Paragraph(text));
//            document.close();
//            Toast.makeText(this, fileName + " đã được lưu ở " + filePath, Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    private void exportTXT() {
        //get text
        String text = editText.getText().toString();
        String dateString = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String fileName = "VASR_" + dateString + ".txt";
        String filePath = Environment.getExternalStorageDirectory() + File.separator + fileName;

        try {
            File file = new File(filePath);
            FileWriter writer = new FileWriter(file);
            writer.append(text);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Tập tin văn bản đã được lưu tại: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //handle user click on button back
        if (id == android.R.id.home) {
            //close activity
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
