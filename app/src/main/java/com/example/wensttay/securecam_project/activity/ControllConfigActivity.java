package com.example.wensttay.securecam_project.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.inputFilter.CamNumberInputFilter;

public class ControllConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_config);
        setTitle("Configuração do Controle");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText camCodeInput = (EditText) findViewById(R.id.camCodeInput);
        camCodeInput.setFilters(new InputFilter[]{
                new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(13),
                new CamNumberInputFilter()
        });
    }

    public void showRecorListPage(View view){
        Intent intent = new Intent(this, SelectRecordActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
