package com.example.myfilemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
private String strFilePath;
    public String ParentdirPath ;
    private String copyPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager_layout);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    class TextAdapter extends BaseAdapter {
        private List<String> data = new ArrayList<>();

        private boolean[] selection;
        public void setData(List<String> data){
            if(data !=null){
                this.data.clear();
                if(data.size()>0){
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        void setSelection(boolean[] selection){
            if(selection != null){
                this.selection = new boolean[selection.length];
                for(int i =0;i<selection.length;i++){
                    this.selection[i] = selection[i];
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            strFilePath =item;
            holder.info.setText(item.substring(item.lastIndexOf('/')+1) );
            if(selection!=null){
                if(selection[position]){
                    holder.info.setBackgroundColor(Color.LTGRAY);
                }else{
                    holder.info.setBackgroundColor(Color.WHITE);
                }
            }
            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView info){
                this.info =info;
            }
        }
    }

    private static  final int REQUEST_PERMISSIONS =1234;
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private  static final int PERMISSION_COUNT =2;


    //for checking permisn on runtym
    @SuppressLint("NewApi")
    private  boolean arePermissionsDenied(){

            int p=0;
            while(p<PERMISSION_COUNT){
                if(checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED){
                    return  true;
                }
                p++;
            }

        return false;
    }

    private boolean isFileManagerInitialized  ;

    private boolean[] selection;
private boolean isLongClick;
    private File[] files;
    private  List<String> filesList;
    private int filesFoundCount;
    private ImageButton refreshButton;
 private  File dir;
private String currentPath;
private int selectedItemIndex;



    @Override
    protected  void  onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && arePermissionsDenied()) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if(!isFileManagerInitialized){
            currentPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                ParentdirPath =currentPath;
//            Intent intent = getIntent();
//            currentPath  = String.valueOf(intent.getIntExtra("currentPath",0));

            //*********************  to access files of the directry nd list all in listView

           final String rootPath =  currentPath.substring(0,currentPath.lastIndexOf('/'));
    //       currentPath =currentPath.substring(0,currentPath.lastIndexOf('/'));

            dir  =new File(currentPath);
            files = dir.listFiles();
            final TextView pathOutput = findViewById(R.id.pathOutput);
            pathOutput.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
              filesFoundCount = files.length;

            final ListView listView = findViewById(R.id.lv);
            final TextAdapter textAdapter1 = new TextAdapter();
            listView.setAdapter(textAdapter1);

              filesList = new ArrayList<>();

            for(int i=0 ; i<filesFoundCount;i++){
                filesList.add(files[i].getAbsolutePath());
            }

            textAdapter1.setData(filesList);

            selection = new boolean[files.length];

            refreshButton = findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    files= dir.listFiles();
                    if(files==null){
                        return;
                    }
                    filesFoundCount=files.length;
                    filesList.clear();
                    for(int i=0 ; i<filesFoundCount;i++){
                        filesList.add(files[i].getAbsolutePath());
                    }
                    textAdapter1.setData(filesList);
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {

                    new Handler().postDelayed(new Runnable() {  // to distinguish b/w long click and normal click
                        @Override
                        public void run() {
                       if(!isLongClick){
                           if(position>files.length){
                               return;
                           }
                           if(files[position].isDirectory()){
                               currentPath = files[position].getAbsolutePath();
                               dir = new File(currentPath);
                               pathOutput.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                               refreshButton.callOnClick();
                               selection = new boolean[files.length];
                               textAdapter1.setSelection(selection);
                           //    Toast.makeText(MainActivity.this,"abcd ", Toast.LENGTH_SHORT).show();
                           }else{



                     Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    File file = new File(strFilePath);

                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                   // String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                    String ext =strFilePath.substring(strFilePath.lastIndexOf('.')+1);
                    //Toast.makeText(MainActivity.this,"Extension: "+ ext, Toast.LENGTH_SHORT).show();
                    String type = mime.getMimeTypeFromExtension(ext);
                    intent.setDataAndType(Uri.fromFile(file), type);
                    startActivity(intent);


                           }
                         }
                        }
                    },50);


                }
            });


            final ImageButton backBtn = findViewById(R.id.goback);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentPath.equals(rootPath)){
                        return;
                    }

                    currentPath = currentPath.substring(0,currentPath.lastIndexOf('/'));
                    dir = new File(currentPath);
                    pathOutput.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                    refreshButton.callOnClick();
                     selection = new boolean[files.length];
                     textAdapter1.setSelection(selection);
                }
            });




            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    isLongClick=true;
                    selection[position] = !selection[position]; // if it is selected unselect it and vice versa
                    textAdapter1.setSelection(selection);
                    int selectionCount =0;
                    for (boolean b : selection) {  //to show button bar if atleat one item is selectd
                        if (b) {
                            selectionCount++;
                        }
                    }
                    if(selectionCount>0){
                        selectedItemIndex = position;
                        findViewById(R.id.buttonBar).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.buttonBar).setVisibility(View.GONE);
                    }
                    new Handler().postDelayed(new Runnable() {   // to distinguish b/w long click and normal click
                        @Override
                        public void run() {
                            isLongClick=false;
                        }
                    },1000);
                    return false;
                }
            });



            final ImageButton deleteBtn = findViewById(R.id.b1);
            final Button copyBtn = findViewById(R.id.copy);
            final Button pasteBtn = findViewById(R.id.paste);


            copyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    copyPath= files[selectedItemIndex].getAbsolutePath();
                    selection = new boolean[files.length];
                    textAdapter1.setSelection(selection);
                    pasteBtn.setVisibility(View.VISIBLE);


                }
            });

            pasteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pasteBtn.setVisibility(View.GONE);
                    String dstPath = currentPath + copyPath.substring( copyPath.lastIndexOf('/'));
                    copy(new File(copyPath) , new File(dstPath));
                    files = new File(currentPath).listFiles();
                    selection = new boolean[files.length];
                    textAdapter1.setSelection(selection);
                    refreshButton.callOnClick();
                    findViewById(R.id.buttonBar).setVisibility(View.GONE);
                }
            });


            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //for delete button
                    for(int i=0;i<files.length;i++){
                        if(selection[i]){
                            delete(files[i]);
                            selection[i]=false;
                        }
                    }
                   refreshButton.callOnClick();
                    selection = new boolean[files.length];
                    textAdapter1.setSelection(selection);
                    findViewById(R.id.buttonBar).setVisibility(View.GONE);
                }
            });


            isFileManagerInitialized =true;
        }else{
            refreshButton.callOnClick();
        }
    }






    private  void copy(File src , File dst){
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] b = new byte[1024];
            int len;
            while ((len=in.read(b))>0){
                out.write(b,0,len);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void delete(File fileorFolder){   // to delete selected item file or folder
        if(fileorFolder.isDirectory()){
            if(fileorFolder.list().length==0){  // if it is a single file
                fileorFolder.delete();
            }else{ // if it is a folder
                String files[] = fileorFolder.list();
                for (String x: files){
                    File fileDelete = new File(fileorFolder,x);
                    delete(fileDelete);
                }
                if(fileorFolder.list().length==0){
                    fileorFolder.delete();
                }
            }
        }else{
            fileorFolder.delete();
        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permission,final int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permission,grantResults);

        if(requestCode == REQUEST_PERMISSIONS && grantResults.length>0){
            if(arePermissionsDenied()){
                //this will keep asking permission
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }else{
                onResume();
            }
        }
    }
}