package co.getdere.otherClasses;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import co.getdere.MainActivity;
import co.getdere.R;
import co.getdere.models.Users;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MyJavaFCM extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID = "admin_channel";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final Context something = this;

        final Intent intent = new Intent(this, MainActivity.class);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users/" + remoteMessage.getData().get("initiator") + "/profile");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users initiatingUser = dataSnapshot.getValue(Users.class);

                SimpleTarget<Bitmap> bitmap = Glide.with(something)
                        .asBitmap()
                        .load(initiatingUser.getImage())
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

//                                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.no_dread);

                                Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(something, ADMIN_CHANNEL_ID)
                                        .setSmallIcon(R.drawable.dere_logo)
                                        .setLargeIcon(resource)
                                        .setContentTitle(remoteMessage.getData().get("title"))
                                        .setContentText(remoteMessage.getData().get("message"))
                                        .setAutoCancel(true)
                                        .setSound(notificationSoundUri)
                                        .setColor(ContextCompat.getColor(something, R.color.green600))
//                                        .setColorized(true)
                                        .setContentIntent(pendingIntent);

                                //Set notification color to match your app color template
//                                notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
                                notificationManager.notify(notificationID, notificationBuilder.build());

                            }
                        });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to devie notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }


}
