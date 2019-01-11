# Actions on Google: Digital Goods Sample using the Java Client Library

This sample demonstrates Actions on Google features for use on Google Assistant including the Digital Purchase API -- using the [Java/Kotlin library for Actions on Google](https://github.com/actions-on-google/actions-on-google-java) and deployed on [App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).
## Setup Instructions

### Digital Goods Requirements
+ You must own a web domain.
    + The domain owner will receive an email to verify ownership via [Google Search Console](https://search.google.com/search-console/welcome)
+ You must have a Play Console developer account: [**sign up**](https://play.google.com/apps/publish/signup/)
+ You must have an Android APK.
    + This sample uses [android-play-billing](https://github.com/googlesamples/android-play-billing/tree/master/TrivialDriveKotlin) Kotlin sample app on Github.
    + Follow the Kotlin README for the setup details.
    + Read about generating a keystore in [Android Studio](https://developer.android.com/studio/publish/app-signing.html#generate-key).
    + Must do an Alpha release at minimum
+ You can only test digital goods on Android devices.
    + Google Assistant installed alongside a payment method set up for your Google account.
    + **Note**: All purchases are done in Sandbox mode by default.
+ Brand verification must be completed before testing (website and Android app state `Connected` in Actions console).

### Configuration
#### Actions Console
1. From the [Actions on Google Console](https://console.actions.google.com/), add a new project > **Create Project** > under **More options** > **Conversational**
1. In the Actions console, from the left navigation menu under **Deploy** > fill out **Directory Information**, where all of the information is required to run transactions (sandbox or otherwise) unless specifically noted as optional.
    + **Additional information** >
        + Do your Actions use the Digital Purchase API to perform transactions of digital goods? > **Yes** > **Save**.
1. Under **Advanced Options** > **Brand verification** > select **</>** to verify your website. Once the status is `Connected` then can connect an Android app.
1. In the [Google Play Developer Console](https://play.google.com/apps/publish) > **Development tools** > **Services & APIs** > **App Indexing from Google Search** > **Verify Website** button. Once you've verified your site it will take up to 24hrs for *Brand verification* reflect this change in the Actions console, nonetheless move on to the next step.
1. Back in the Actions console, from the left navigation menu under **Build** > **Actions** > **Add Your First Action** > **BUILD** (this will bring you to the Dialogflow console) > Select language and time zone > **CREATE**.
1. In the Dialogflow console, go to **Settings** ⚙ > **Export and Import** > **Restore from zip** using the `agent.zip` in this sample's directory.

### Service Account Authentication with JWT/OAuth 2.0
1. In the [Google Cloud Platform console](https://console.cloud.google.com/), select your *Project ID* from the dropdown > **Menu ☰** > **APIs & Services** > **Library**
1. Select **Actions API** > **Enable**
1. Under **Menu ☰** > **APIs & Services** > **Credentials** > **Create Credentials** > **Service Account Key**.
1. From the dropdown, select **New Service Account**
    + name:  `credentials`
    + role:  **Project/Owner**
    + key type: **JSON** > **Create**
    + Your private JSON file will be downloaded to your local machine; save as `credentials.json` in `src/main/java/resources`

#### App Engine Deployment & Webhook Configuration
1. Replace `'PACKAGE_NAME'` in `src/main/java/com/example/billing/DigitalGoodsService.js` with the package name of your Android app.
1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task.

#### Dialogflow Console
Return to the [Dialogflow Console](https://console.dialogflow.com), from the left navigation menu under **Fulfillment** > **Enable Webhook**, set the value from the previous step of **URL** to `https://${YOUR_PROJECT_ID}.appspot.com` > **Save**.
1. From the left navigation menu, click **Integrations** > **Integration Settings** under Google Assistant > Enable **Auto-preview changes** >  **Test** to open the Actions on Google simulator then say or type `Talk to my test app`.

### Running this Sample
+ You can test your Action on any Google Assistant-enabled device on which the Assistant is signed into the same account used to create this project. Just say or type, “OK Google, talk to my test app”.
+ You can also use the Actions on Google Console simulator to test most features and preview on-device behavior.
1. Follow the instructions below to test a transaction.
1. To test payment when confirming transaction, uncheck the box in the Actions
console simulator indicating testing in Sandbox mode.

### References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/) or [Support](https://developers.google.com/actions/support/).
+ For bugs, please report an issue on Github.
+ Actions on Google [Documentation](https://developers.google.com/actions/extending-the-assistant)
+ [Webhook Boilerplate Template](https://github.com/actions-on-google/dialogflow-webhook-boilerplate-java) for Actions on Google.
+ More info about [Gradle & the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle).
+ More info about deploying [Java apps with App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).

### Make Contributions
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

### License
See [LICENSE](LICENSE).

### Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).