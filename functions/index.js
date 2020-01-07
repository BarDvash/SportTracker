const admin = require('firebase-admin');
const functions = require('firebase-functions');
admin.initializeApp(functions.config().firebase);

const functionTriggers = functions.region('europe-west1').firestore; //TODO: check where out firestore located
const db = admin.firestore();



exports.trainee_ask_trainer_push_notification = functions.firestore.document('/business_users/{trainer_id}/requests/{trainee_id}').onCreate((snap, context) => {
	const trainer_id = context.params.trainer_id;
	const trainee_id = context.params.trainee_id;
	
    console.log('Push notification event triggered');

    // Create a notification
    const payload = {
        notification: {
			title: 'new pending request',
			body:'a trainee want you to be his personal trainer',
            sound: "default"
        },
    };

    //Create an options object that contains the time to live for the notification and the priority
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    return admin.messaging().sendToTopic("trainee_ask_trainer_push_notification"+trainer_id, payload, options);
});



   exports.trainer_accept_trainee_request_push_notification = functions.firestore.document('/business_users/{trainer_id}/customers/{trainee_id}').onCreate((snap, context) => {
   	const trainer_id = context.params.trainer_id;
   	const trainee_id = context.params.trainee_id;

       console.log('Push notification event triggered');

       // Create a notification
       const payload = {
           notification: {
   			title: 'request accepted',
   			body:'you have new personal trainer!',
               sound: "default"
           },
       };

       //Create an options object that contains the time to live for the notification and the priority
       const options = {
           priority: "high",
           timeToLive: 60 * 60 * 24
       };

       return admin.messaging().sendToTopic("trainer_accept_trainee_request_push_notification"+trainee_id, payload, options);
   });


// done when trainer accept trainee request to be his personal trainer : notify trainer
   exports.you_have_new_customer = functions.firestore.document('/business_users/{trainer_id}/customers/{trainee_id}').onCreate((snap, context) => {
   	const trainer_id = context.params.trainer_id;
   	const trainee_id = context.params.trainee_id;

       console.log('Push notification event triggered');

       // Create a notification
       const payload = {
           notification: {
   			title: 'request accepted',
   			body:'you have new customer!',
               sound: "default"
           },
       };

       //Create an options object that contains the time to live for the notification and the priority
       const options = {
           priority: "high",
           timeToLive: 60 * 60 * 24
       };

       return admin.messaging().sendToTopic("you_have_new_customer"+trainer_id, payload, options);
   });



exports.pending_request_home_screen1 = functions.firestore.document('/business_users/{trainer_id}/requests/{trainee_id}').onCreate((snap, context) => {
	const trainer_id = context.params.trainer_id;
	const trainee_id = context.params.trainee_id;

	return db.doc('/business_users/' + trainer_id + '/notifications/pending_requests').set({notification: 'you have new pending requests'});
});

exports.pending_request_home_screen2 = functions.firestore.document('/regular_users/{trainee_id}/requests/{trainer_id}').onCreate((snap, context) => {
	const trainer_id = context.params.trainer_id;
	const trainee_id = context.params.trainee_id;

	return db.doc('/regular_users/' + trainee_id + '/notifications/pending_requests').set({notification: 'you have new pending requests'});
});











