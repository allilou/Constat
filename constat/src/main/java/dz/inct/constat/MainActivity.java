package dz.inct.constat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerType;
    private Spinner spinnerNature;
    private Spinner spinnerEtat;
    private ImageView photo1View;
    private ImageView photo2View;
    private TextView LongitudeText;
    private TextView LatitudeText;
    private TextView AltitudeText;
    private EditText numText;
    private EditText operateurText;
    private EditText remarqueText;

    private Button updatePosButton;


    private LocationManager locationManager;
    private LocationListener listener;

    private String photo1FileName = "";
    private String photo2FileName = "";
    private String photo2FilePath = "";
    private String photo1FilePath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerType = (Spinner) findViewById(R.id.repere_type_chooser);
        spinnerNature = (Spinner) findViewById(R.id.repere_nature_chooser);
        spinnerEtat = (Spinner) findViewById(R.id.repere_etat_chooser);
        numText = (EditText) findViewById(R.id.num_text);
        remarqueText = (EditText) findViewById(R.id.remarques_text);

        photo1View = (ImageView) findViewById(R.id.photo1);
        photo2View = (ImageView) findViewById(R.id.photo2);

        LongitudeText = (TextView) findViewById(R.id.longitude_text);
        LatitudeText = (TextView) findViewById(R.id.latitude_text);
        AltitudeText = (TextView) findViewById(R.id.altitude_text);

        updatePosButton = (Button) findViewById(R.id.update_position);

        operateurText = (EditText) findViewById(R.id.operateur);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this,
                R.array.type_repere, android.R.layout.simple_spinner_item);

        // Apply the adapter to the spinner
        spinnerType.setAdapter(adapterType);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterNature = ArrayAdapter.createFromResource(this,
                R.array.nature_repere, android.R.layout.simple_spinner_item);

        // Apply the adapter to the spinner
        spinnerNature.setAdapter(adapterNature);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterEtat = ArrayAdapter.createFromResource(this,
                R.array.etat_repere, android.R.layout.simple_spinner_item);

        // Apply the adapter to the spinner
        spinnerEtat.setAdapter(adapterEtat);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LongitudeText.setText(" " + location.convert(location.getLongitude(), location.FORMAT_SECONDS));
                LatitudeText.setText(" " + location.convert(location.getLatitude(), location.FORMAT_SECONDS));
                AltitudeText.setText(" " + location.convert(location.getAltitude(), location.FORMAT_SECONDS));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
        erase_form();
    }

    // Creation of the menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    // Selection on the menu bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:
                erase_form();
                Toast.makeText(this, "Formulaire effacé", Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_save_html:
                save_data_to_html();

                break;

            default:
                break;
        }

        return true;
    }

    // Save the recorded data in an html file
    private void save_data_to_html() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        String content = generate_html_file();

        String num = numText.getText().toString();

        if (num.length() == 0) {
            numText.setError("Vous devez indiquer la référence du repère !");
            return;
        }
        String fileName = "Fiche_" + num + ".html";

        try {
            String folder_inct = "INCT";

//            File f = new File(Environment.getExternalStorageDirectory(), folder_inct);
//            if (!f.exists()) {
//                f.mkdirs();
//            }
//            File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_inct, fileName);

            File file = new File(getExternalFilesDir("fiches_constat"), fileName);

            if (file.exists()) {
                Toast.makeText(this, "La fiche de ce repère exite déjà !!", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
            Toast.makeText(this, "Fiche enrgistrées dans le fichier" + file.getAbsolutePath(), Toast.LENGTH_LONG)
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
        }

    }

    // generate an html file from the inputs
    private String generate_html_file() {
        String content = "<!DOCTYPE html><html>";
        content += "<head><meta charset=\"UTF-8\"> <title>Fiche de constat</title>";

        content += "<style> " +
                "table, th, td { border: 1px solid black; border-collapse: collapse; font-family:arial;}" +
                "th, td { padding: 5px; text-align:center} td { text-align:center} </style>";
        content += "</head><body>";

        content += "<h1  style=\"text-align:center; font-family:arial;\" > FICHE DE CONSTAT (FRM-IG0-31) </h1></br>";

        content += "<table style=\"width:100%\">";
        content += "<tr> <th> Numéro du repère </th><th> Type de repère </th><th> Nature du repère </th>";
        content += "<tr> <td>" + numText.getText().toString() + "</td>";
        content +=      "<td>" + spinnerType.getSelectedItem().toString() + "</td>";
        content +=      "<td>" + spinnerNature.getSelectedItem().toString() + "</td></tr>";

        content += "<tr><th> Date d'observation </th><th> Opérateur </th><th> Etat </th> </tr>";
        content += "<tr><td>" + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()) + "</td>";
        content +=     "<td>" + operateurText.getText().toString() + "</td>";
        content +=     "<td>" + spinnerEtat.getSelectedItem().toString() + "</td></tr>";
        content += "</table ></br>";

        content += "<h3  style=\"text-align:center;; font-family:arial;\" > Coordonnées approchées </h3>";
        content += "<table style=\"width:100%;\">";
        content += "<tr> <th> Longitude </th> <td>" + LongitudeText.getText() + "</td>";
        content +=      "<th> Latitude </th>  <td>" + LatitudeText.getText()  + "</td>";
        content +=     " <th> Altitude </th>  <td>" + AltitudeText.getText()  + "</td></tr>";
        content += "</table ></br>";

        content += "<h3  style=\"text-align:center;; font-family:arial;\" > Photos </h3>";
        content += "<table style=\"width:100%;\">";
        content += "<tr><td> <img src=\""+photo1FileName+"\" alt=\"Photo 1\" height=\"400\" width=\"400\"> </td>";
        content += "    <td> <img src=\""+photo2FileName+"\" alt=\"Photo 2\" height=\"400\" width=\"400\"> </td></tr>";
        content += "</table ></br>";

        content += "<h3  style=\"text-align:center; font-family:arial;\" > Remarques </h3>";
        content += "<table style=\"width:100%; ;\">";
        content += "<tr> <td> <pre style=\"font-family:arial; text-align:left\">"
                      + remarqueText.getText().toString() + "</pre></td></tr>";
        content += "</table >";

        content += "</body> </html>";

        return content;
    }

    // Initialze the form by erasing all the data
    private void erase_form() {
        spinnerType.setSelection(0);
        spinnerNature.setSelection(0);
        spinnerEtat.setSelection(0);
        numText.setText("");
        remarqueText.setText("");
        LatitudeText.setText("DMS");
        LongitudeText.setText("DMS");
        AltitudeText.setText("DMS");

        this.photo1View.setImageResource(R.drawable.borne);
        this.photo2View.setImageResource(R.drawable.borne);

        photo2FileName = "";
        photo2FileName = "";
        photo1FilePath = "";
        photo2FilePath = "";
    }


    // Handling results for location permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }


    // setting up location listener when updating the position
    private void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

        // this code won'textView execute IF permissions are not allowed, because in the line above there is return statement.
        updatePosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
            }
        });
    }


    // taking a photo1 with the camera
    public void photo1Click(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Erreur de création du fichier image ", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "dz.inct.android.fileprovider",
                        photoFile);

                photo1FileName = photoFile.getName();
                photo1FilePath = photoFile.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, 1);
            }
        }
    }

    // taking a photo2 with the camera
    public void photo2Click(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Erreur de création du fichier image ", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "dz.inct.android.fileprovider",
                        photoFile);

                photo2FileName = photoFile.getName();
                photo2FilePath = photoFile.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, 2);
            }
//            Toast.makeText(this, "uri "+uriPhoto.toString(), Toast.LENGTH_SHORT).show();
//            startActivityForResult(intent, 2);
        }
    }

    // create a temporary image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalFilesDir("fiches_constat");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    // Handling the images after camera call
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK ) {
            return;
        }
//
//        if (data.hasExtra("data")) {
//            Bundle extras = data.getExtras();
//            Bitmap photoBitmap  = (Bitmap) extras.get("data");
//
//            if (requestCode == 1){
//                photo1View.setImageBitmap(photoBitmap);
//            }
//
//            else if (requestCode == 2) {
//                photo2View.setImageBitmap(photoBitmap);
//            }
//
//        }
//            Toast.makeText(this, "No data " + data.toString(), Toast.LENGTH_LONG).show();

            if (requestCode == 1) {
                if (!photo1FilePath.isEmpty()) {
                    Bitmap photoBitmap = BitmapFactory.decodeFile(photo1FilePath);
                    photo1View.setImageBitmap(photoBitmap);
                }

            } else if (requestCode == 2) {
                if (!photo2FilePath.isEmpty()) {
                    Bitmap photoBitmap = BitmapFactory.decodeFile(photo2FilePath);
                    photo2View.setImageBitmap(photoBitmap);
                }
            }

    }//On ActivityResult

}  //class
