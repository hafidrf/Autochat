package id.co.kamil.autochat;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.MainActivity.MAIN_RECEIVER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_FLOATING_WIDGET;


public class FloatingViewService extends Service {
  private WindowManager mWindowManager;
  private View mFloatingView;
  private SharPref sharePref;

  public FloatingViewService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    sharePref = new SharPref(getApplicationContext());
    //Inflate the floating view layout we created
    mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

    int LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    //Add the view to the window.
    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        LAYOUT_FLAG,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

    //Specify the view position
    params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
    params.x = 0;
    params.y = 100;

    //Add the view to the window
    mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    mWindowManager.addView(mFloatingView, params);

    //The root element of the collapsed view layout
    final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
    //The root element of the expanded view layout
    final View expandedView = mFloatingView.findViewById(R.id.expanded_container);

    //Set the close button
    final ImageView closeButtonCollapsed = mFloatingView.findViewById(R.id.close_btn);
    closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sharePref.createSession(STATUS_FLOATING_WIDGET,false);
        //close the service and remove the from from the window
        stopSelf();
      }
    });

    //Set the view while floating view is expanded.
    //Set the message pending button.
    TextView messagePendingButton = mFloatingView.findViewById(R.id.buttonMessagePending);
    messagePendingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //Open the application  click.
        Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
        Intent pushNotification = new Intent(MAIN_RECEIVER);
        pushNotification.putExtra("action", "antrian");
        intent.putExtra(MainActivity.TAG_OPEN_MENU, pushNotification);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        //close the service and remove view from the view hierarchy
        stopSelf();
      }
    });

    final Switch switchEnabledBulkSender = mFloatingView.findViewById(R.id.switchEnabledBulkSender);
    boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);
    switchEnabledBulkSender.setChecked(status_bulk_sender);
    switchEnabledBulkSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sharePref.createSession(STATUS_BULK_SENDER,isChecked);
      }
    });

    //Set the close button
    ImageView closeButton = mFloatingView.findViewById(R.id.close_button);
    closeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
      }
    });


    //Open the application on thi button click
    final ImageView openButton = mFloatingView.findViewById(R.id.open_button);
    openButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //Open the application  click.
        Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        //close the service and remove view from the view hierarchy
        stopSelf();
      }
    });

    //Drag and move floating view using user's touch action.
    mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;


      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:

            //remember the initial position.
            initialX = params.x;
            initialY = params.y;

            //get the touch location
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
            return true;
          case MotionEvent.ACTION_UP:
            int Xdiff = (int) (event.getRawX() - initialTouchX);
            int Ydiff = (int) (event.getRawY() - initialTouchY);


            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
            //So that is click event.
            if (Xdiff < 10 && Ydiff < 10) {
              if (isViewCollapsed()) {
                //When user clicks on the image view of the collapsed layout,
                //visibility of the collapsed layout will be changed to "View.GONE"
                //and expanded view will become visible.
                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);

                boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);
                switchEnabledBulkSender.setChecked(status_bulk_sender);
              }
            }
            return true;
          case MotionEvent.ACTION_MOVE:
            //Calculate the X and Y coordinates of the view.
            params.x = initialX + (int) (event.getRawX() - initialTouchX);
            params.y = initialY + (int) (event.getRawY() - initialTouchY);


            //Update the layout with new X & Y coordinate
            mWindowManager.updateViewLayout(mFloatingView, params);
            return true;
        }
        return false;
      }
    });
  }


  /**
   * Detect if the floating view is collapsed or expanded.
   *
   * @return true if the floating view is collapsed.
   */
  private boolean isViewCollapsed() {
    return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
  }
}
