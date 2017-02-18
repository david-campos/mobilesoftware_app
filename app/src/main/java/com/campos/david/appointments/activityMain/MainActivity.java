package com.campos.david.appointments.activityMain;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.campos.david.appointments.AboutActivity;
import com.campos.david.appointments.ApiUrlDialog;
import com.campos.david.appointments.R;
import com.campos.david.appointments.activityNewAppointment.NewAppointmentActivity;
import com.campos.david.appointments.activitySettings.SettingsFragment;
import com.campos.david.appointments.model.DBContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        FloatingActionButton fabNewAppointment =
                (FloatingActionButton) findViewById(R.id.fab_newAppointment);

        if (viewPager != null && tabLayout != null) {
            // Set PagerAdapter so that it can display items
            viewPager.setAdapter(new MainActivityPagerAdapter(getSupportFragmentManager(),
                    MainActivity.this));
            viewPager.setCurrentItem(1);
            // Give the TabLayout the ViewPager
            tabLayout.setupWithViewPager(viewPager);
        }

        if (fabNewAppointment != null) {
            fabNewAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newAppointment = new Intent(getApplicationContext(), NewAppointmentActivity.class);
                    startActivity(newAppointment);
                }
            });
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                // Display the settings as the main content.
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingsFragment())
                        .commit();
                return true;
            case R.id.action_about:
                Intent throwAboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(throwAboutIntent);
                return true;
            case R.id.action_change_ip:
                ApiUrlDialog dialog = new ApiUrlDialog();
                dialog.show(getFragmentManager(), "NoticeDialogFragment");
                return true;
            case R.id.action_bd_dummy_data: {
                Log.d(TAG, "Generating dummy data");
                String[] names = {"Josh", "Myrthe", "Kristin", "Martyna", "Belen", "Mauro", "Julien", "Henrik", "Nik", "Joanna"};
                String[] apps = {"Some beers", "Julien's birthday", "Romantic dinner", "Studying night", "Giraffatitan", "Coelophysis", "Xiaosaurus", "Cedarpelta", "Elrhazosaurus", "Taveirosaurus", "Agujaceratops", "Tawa", "Futalognkosaurus", "Shamosaurus", "Lophostropheus", "Histriasaurus", "Brontosaurus", "Proceratosaurus", "Vulcanodon", "Mymoorapelta", "Sauraechinodon", "Propanoplosaurus", "Banji", "Oryctodromeus", "Zhuchengceratops", "Narambuenatitan", "Ojoceratops", "Liliensternus", "Rhabdodon", "Puertasaurus", "Fulengia", "Priodontognathus", "Asiaceratops", "Tangvayosaurus", "Montanoceratops", "Animantarx", "Microvenator", "Archaeodontosaurus", "Telmatosaurus", "Scaphonyx", "Rhadinosaurus", "Nanshiungosaurus", "Silesaurus", "Sinusonasus", "Clepsysaurus", "Australovenator", "Wellnhoferia", "Gryponyx", "Asiaceratops", "Peishansaurus", "Heptasteornis", "Maxakalisaurus", "Raptorex", "Lamaceratops", "Tsintaosaurus", "Pachyspondylus", "Ajkaceratops", "Avimimus", "Ornithotarsus", "Zhuchengtyrannus", "Ornatotholus", "Clarencea", "Anodontosaurus", "Andesaurus", "Zanabazar", "Leyesaurus", "Oplosaurus", "Spinostropheus", "Stenopelix", "Basutodon", "Vectisaurus", "Seitaad", "Othnielia", "Kayentavenator", "Euoplocephalus", "Andesaurus", "Epanterias", "Pectinodon", "Coelosaurus", "Veterupristisaurus", "Cystosaurus", "Orodromeus", "Podokesaurus", "Albisaurus", "Orinosaurus", "Shaochilong", "Oryctodromeus", "Aepisaurus", "Agujaceratops", "Saurornithoides", "Dolichosuchus", "Triceratops", "Tethyshadros", "Shidaisaurus", "Sinotyrannus", "Actiosaurus", "Acrotholus", "Geranosaurus", "Jiutaisaurus", "Lagosuchus", "Palaeoctonus", "Amtosaurus", "Berberosaurus", "Wuerhosaurus"};
                String[] places = {"Biblioteka", "Niebostan", "Tango Steak House", "Lordys", "Guangzhou", "Fuzhou", "Beijing", "Baotou", "Hohhot", "Guiyang", "Yinchuan", "Nanjing", "Changzhou", "Chuzhou", "Hefei", "Jinan", "Qingdao", "Harbin", "Zhaodong", "Taiyuan", "Xi'an", "Xianyang", "Shenzhen", "Nanning", "Zhengzhou", "Xinxiang", "Luohe", "Luoyang", "Chaoyang", "Xingyi", "Foshan", "Haikou", "Chengdu", "Dongguan", "Mingzhou", "Chongqing", "Zhuhai", "Kunming", "Wuhan", "Xiling", "Huizhou", "Jiangmen", "Shantou", "Changxiacun", "Zhongshan", "Lhasa", "Nanchang", "Tianjin", "Shanghai", "Hebei", "Shijiazhuang", "Quanzhou", "Putian", "Xiamen", "Chengyang", "Zhangzhou", "Sanming", "Nanping", "Baoding", "Langfang", "Yantai", "Binzhou", "Lanzhou", "Yueqing", "Zhongxin", "Zhoushan", "Hangzhou", "Ningbo", "Wenzhou", "Changchun", "Fuyang", "Jieshou", "Anqing", "Wuhu", "Shishi", "Shishi", "Weitang", "Shenyang", "Changsha", "Yongjiawan", "Lengshuijiang", "Shijiazhuang", "Xuchang", "Suzhou", "Xuzhou", "Taizhou", "Nanyang", "Xinhua", "ÃœrÃ¼mqi", "Yan'an Beilu", "Baotao", "Macao", "Wuxi", "Yangzhou", "Baiyin", "Tongren", "Kunshan", "Zhangjiagang", "Jiangyin", "Zhenjiang", "Zhoukou", "Anyang", "Dalian", "Tongcun", "Shihezi", "Xining", "Zhangye", "Qingyang", "Wangsu", "Anshun", "Kaili", "Bijie", "Shuigang", "Jianyuan", "Jinlong", "Jingdezhen", "Yichang", "Xiangfan", "Jiayuan", "Shashi", "Yingjie", "Huangshi", "Jinhua", "Zhengyuan", "Langli", "Hengyang", "Jianning", "Xiangtan", "Rongxing", "Xincao", "Jinda", "Nanlong", "Xiangcheng", "Nanma", "Zhongxiang", "Pudong", "Shuitou", "Zhenxing", "Jinjiang", "Longxun", "Guanqiao", "Jingfeng", "Shijing", "Tangbian", "Jiangchuanlu", "Jiaoyun", "Guizhoumanzuxiang", "Qingzhen", "Changde", "Xianning", "Jiaozhou", "Weifang", "Tai'an", "Luoxi", "Guoji", "Guangdong", "Sijiqing", "Huzhou", "Panjin Shi", "Daqing", "Jilin City", "Lianyungang", "Yancheng", "Yuehu", "Kunshan", "Taicang", "Lianshui", "Nantong", "Jiaotong", "Changshu City", "Xingxiangcun", "Jinsha", "Jiangyan", "Chaigoubu", "Ma'anshan", "Huainan", "Haibei", "Shenlong", "Nangxian", "Rongsheng", "Changfeng", "Chengqiao", "Jiafu", "Shenzhou", "Shantou", "Qingyuan", "Gyari", "Xinshijie", "Zhaoqing", "Zhanjiang", "Kuicheng", "Taoyuan", "Jincheng", "Caishen", "Shiyan", "Liaoyang", "Xingtai", "Wenchang", "Wanning", "Qionghai", "Huilongba", "Dingcheng", "Baodian", "Wuzhishan", "Chengmai", "Yinggen", "Ledong", "Lincheng", "Baisha", "Changjiang", "Dongfang", "Changjian", "Jinmao", "Yangpu", "Baipo", "Jiefang", "Danzhou", "Lingshui", "Haidian", "Sanya", "Rongjiang", "Longyan", "Jinghe", "Zhangjiakou", "Renqiu", "Yaocheng", "Kaifeng", "Hebi", "Jiaozuo", "Pingdingshan", "Anshan", "Dandong", "Haitang", "Tongchuan", "Ankang", "Guozhen", "Shangluo", "Xijing", "Weinan", "Yulin", "Yining", "Dingxi", "Wuwei", "Dawukou", "Lishui", "Quzhou", "Hejiang", "Handan", "Qinhuangdao", "Hengshui", "Longxin", "Wen'an", "Yichun", "Heihe", "Jiaxing", "Korla", "Kuytun", "Di'an", "Yu'an", "Mengzhou", "Hulu", "Yizhou", "Shuliang", "Shancheng", "Fushun", "Dashiqiao", "Laonian", "Shengli", "Wenquan", "Zhiye", "Lingzhi", "Zhongtang", "Gucheng", "Xinhua", "Ninghe", "Dangyang", "Yizhong", "Jizhou", "Tianbao", "Jinghai", "Julong", "Jiaqin", "Jiayue", "Dabaizhuang", "Juchuan", "Hexi", "Jinnan", "Hangu", "Nankai", "Hedong", "Yanglou", "Huawei", "Hanting", "Tianshi", "Baiyu", "Bohai", "Rujia", "Tongwang", "Meijiang", "Dagang", "Baodi", "Daqiuzhuang", "Yuxi", "Zicheng", "Shaoxing", "Zhoushan", "Xiaoshan", "Linhai", "Cixi", "Jinchuan", "Zhaobaoshan", "Tiangang", "Beilun", "Zhangqi", "Zhenghai", "Cicheng", "Lishu", "Chengbei", "Heyi", "Xikou", "Jiangkou", "Shunshui", "Simen", "Yuyao", "Lanjiang", "Jiangdong", "Gaotang", "Xiangshan", "Shipu", "Jinyi", "Chengzhong", "Yinzhou", "Luoyang", "Mapai", "Cangnan", "Jinxiangzhen", "Yingjia", "Pingyang", "Rui'an", "Tianfu", "Shangtang", "Yongjia", "Tiancheng", "Hongqiao", "Furong", "Wenxing", "Mingxi", "Jinshan", "Changtu", "Anzi", "Xianren", "Zhongxing", "Guanli", "Yucai", "Xianjiang", "Aojiang", "Dongtou", "Rongjiang", "Jinmen", "Qiantang", "Baojiang", "Huling", "Liushi", "Yuecheng", "Hongyun", "Longhua", "Yajin", "Simcun", "Longgang", "Yingdu", "Wansong", "Yuele", "Nanjiang", "Longhu", "Ningyi", "Fengling", "Wuzhou", "Xinchen", "Jinghu", "Fangzhuang", "Yinfang", "Cili", "Angu", "Feiyun", "Wanquan", "Kunyang", "Shibei", "Jiangnan", "Yujing", "Yishan", "Xuefeng", "Feilong", "Shangrao", "Xuexiao", "Yuzhen", "Huangbao", "Longquan", "Pizhou", "Songyang", "Qingtian", "Chenguang", "Kaiyuan", "Dongsheng", "Jinyun", "Zhongshan", "Miaogao", "Yuanli", "Shinian", "Qingfeng Chengguanzhen", "Liu`an", "Yulong", "Haixing", "Sanjiaocheng", "Pinghu", "Jinling", "Fengming", "Tongxiang", "Puyuan", "Dingqiao", "Yanjiang", "Wutong", "Pingchuan", "Dushu", "Nanxun", "Wuxing", "Yangzhou", "Hongyuan", "Anji", "Shangying", "Deqing", "Digang", "Sanguan", "Yuantong", "Changxin", "Huating", "Putuoshan", "Jinyuan", "Dinghai", "Xiangnan", "Putuo", "Xintian", "Donghuxu", "Zhuji", "Jingcheng", "Jiangtian", "Xingchang", "Jindou", "Xinchang", "Baiyun", "Qianqing", "Tianchang", "Tianchi", "Luzhou", "Qinjiang", "Tianzhu", "Chengguan", "Jinhong", "Huaqiao", "Maotai", "Hezhu", "Dahai", "Shanhu", "Changle", "Guoshang", "Dongshen", "Shangbu", "Zhedong", "Boxing", "Tianyuan", "Guodian", "Linping", "Meicheng", "Jiyang", "Tonglu", "Fuchunjiang", "Qiandaohu", "Yuhang", "Changsheng", "Honglin", "Xiaoheshan", "Binjiang", "Yijin", "Xunxian", "Qianshan", "Zhongzhou", "Chongxian", "Gongchang", "Huangyan", "Jiaojiang", "Wenling", "Xindu", "Sili", "Luqiao", "Baoshan", "Yanjing", "Jinqingzhen", "Gamlung", "Yiwu", "Dongyang", "Yongkang", "Lanxi", "Wuyi", "Wanjia", "Fotang", "Yuhai", "Yiting", "Puyang", "Longfeng", "Yueliangwan", "Renhe", "Yangfen", "Youjia", "Hanshang", "Jindu", "Junping", "Aoma", "Yinliang", "Lijing", "Renhou", "Wangshang", "Pan'an", "Longchuan", "Hengzhou", "Lianyuan", "Jinlun", "Qiaodou", "Shizhu", "Huajie", "Xixi", "Hengdian", "Dongcheng", "Dongdu", "Fusheng", "Yongjin", "Youyi", "Yuchi", "Haiyang", "Tashi", "Jiya", "Zhangqiu", "Shangdong", "Zoucheng", "Jining", "Linyi", "Feixian", "Yishui", "Zaozhuang", "Zibo", "Laiwu", "Jiyang", "Yayu", "Zhenzhuquan", "Changzhi", "Changping", "Daxing", "Fuling", "Xiangyuan", "Shiji", "Changshan", "Shangzhou", "Kaihua", "Jiangshan", "Longzhou", "Citai", "Jinyang", "Yanhai", "Xintai", "Yinjiang", "Guxiang", "Yindian", "Yiwu", "Qujiang", "Juhua", "Zhicheng", "Ningde", "Meizhou", "Shaowu", "Zhanghou"};
                String[] states = {"pending", "accepted", "refused"};
                String[] descriptions = {"Es un hecho establecido hace demasiado tiempo que un lector se distraerá con el contenido del texto de un sitio mientras que mira su diseño. El punto de usar Lorem Ipsum es que tiene una distribución más o menos normal de las letras, al contrario de usar textos como por ejemplo \"Contenido aquí, contenido aquí\". Estos textos hacen parecerlo un español que se puede leer. Muchos paquetes de autoedición y editores de páginas web usan el Lorem Ipsum como su texto por defecto, y al hacer una búsqueda de \"Lorem Ipsum\" va a dar por resultado muchos sitios web que usan este texto si se encuentran en estado de desarrollo.", "Atiam aliquam viverra nisl, nec pretium neque fringilla nec. Proin varius lobortis lorem quis porta. Phasellus egestas nisl sed libero finibus viverra. Praesent dignissim at massa vitae iaculis. Fusce rutrum condimentum molestie. In mattis, metus ac congue mollis, nisl mauris rutrum sem, ut dictum felis turpis sit amet nibh. Nullam ac fermentum mi."};
                ContentValues values = new ContentValues();
                values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "Appointment");
                values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "This is a generic appointment");
                values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "1");
                Uri uriType = getContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
                if (uriType != null) {
                    // Insert 10 users
                    List<Uri> users = new ArrayList<>();
                    for (int i = 1; i <= 10; i++) {
                        users.add(addUser(1000 + i, names[i - 1], Integer.toString(666000000 + i), 0, Math.random() < 0.35));
                    }
                    // Insert 50 random appointments
                    for (int i = 1; i <= 50; i++) {
                        boolean mine = (Math.random() < 0.1);
                        int creatorNumber = (int) Math.round(Math.random() * (users.size() - 1));
                        Integer creator = (mine ? null :
                                Integer.parseInt(users.get(creatorNumber).getLastPathSegment()));
                        Uri app = addAppointment(i, 0, Integer.parseInt(uriType.getLastPathSegment()),
                                creator, Math.random() < 0.2, randomElement(descriptions), randomElement(apps));
                        Uri uri = addProposition(i, randomElement(places), ((new Date()).getTime() + i * 1000000L), Math.random() * 50, Math.random() * 50);
                        // Update proposition
                        values.clear();
                        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, uri.getLastPathSegment());
                        getContentResolver().update(app, values, null, null);
                        // If it is not mine invite me
                        if (!mine) {
                            inviteUser(null, randomElement(states), null, i);
                        }
                        // Invite between 1 and 6 random people, apart from the user
                        int numberOfPeople = (int) Math.round(Math.random() * 5) + (mine ? 1 : 0);
                        ArrayList<Uri> copyUsers = new ArrayList<>(users);
                        for (int j = 0; j < numberOfPeople; j++) {
                            int user;
                            do {
                                user = (int) Math.round(Math.random() * (copyUsers.size() - 1));
                            }
                            while (!mine && copyUsers.get(user).getLastPathSegment().equals(creator.toString()));
                            inviteUser(
                                    Integer.parseInt(copyUsers.get(user).getLastPathSegment()),
                                    randomElement(states), null, i);
                            copyUsers.remove(user);
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private <T> T randomElement(T[] array) {
        return array[(int) Math.round(Math.random() * (array.length - 1))];
    }

    private Uri addAppointment(int id, int proposal, int type, Integer creator, boolean closed,
                               String description, String name) {
        ContentValues values = new ContentValues();
        values.put(DBContract.AppointmentsEntry._ID, id);
        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, proposal); // There is no foreign key check
        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, type);
        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, creator);
        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, closed);
        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, description);
        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, name);
        return getContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
    }

    private Uri addProposition(int appointment, String place, long timestamp, double lat, double lon) {
        ContentValues values = new ContentValues();
        values.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, appointment);
        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, place);
        values.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP, timestamp);
        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, lat);
        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, lon);
        return getContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values);
    }

    private Uri inviteUser(Integer user, String state, Integer reason, int appointment) {
        ContentValues values = new ContentValues();
        values.put(DBContract.InvitationsEntry.COLUMN_USER, user);
        values.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, appointment);
        values.put(DBContract.InvitationsEntry.COLUMN_STATE, state);
        values.put(DBContract.InvitationsEntry.COLUMN_REASON, reason);
        return getContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values);
    }

    private Uri addUser(int id, String name, String phone, int picture, boolean blocked) {
        ContentValues values = new ContentValues();
        values.put(DBContract.UsersEntry._ID, id);
        values.put(DBContract.UsersEntry.COLUMN_NAME, name);
        values.put(DBContract.UsersEntry.COLUMN_PICTURE, picture);
        values.put(DBContract.UsersEntry.COLUMN_PHONE, phone);
        values.put(DBContract.UsersEntry.COLUMN_BLOCKED, blocked);
        return getContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);
    }
}
