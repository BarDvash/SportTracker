const admin = require('firebase-admin');
const functions = require('firebase-functions');
admin.initializeApp(functions.config().firebase);

const functionTriggers = functions.region('europe-west1').firestore; //TODO: check where out firestore located
const db = admin.firestore();


exports.trainee_sent_request = functions.firestore.document('/business_users/{trainer_id}/requests/{trainee_id}').onCreate((snap, context) => {
    const trainer_id = context.params.trainer_id;
    const trainee_id = context.params.trainee_id;

    console.log('Push notification event triggered');


    db.collection('regular_users').doc(trainee_id).get().then((doc) => {

        const trainee_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'New pending request',
                body: trainee_name + ' want you to be his personal trainer!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        return admin.messaging().sendToTopic("trainee_sent_request" + trainer_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });


});


exports.trainer_accepted_trainee_request = functions.firestore.document('/regular_users/{trainee_id}/approved_requests/{trainer_id}').onCreate((snap, context) => {
    const trainer_id = context.params.trainer_id;
    const trainee_id = context.params.trainee_id;

    console.log('Push notification event triggered');

    db.collection('business_users').doc(trainer_id).get().then((doc) => {

        const trainer_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'Request accepted',
                body: trainer_name + ' is your new personal trainer!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };
        let deleteDoc = db.collection('regular_users').doc(trainee_id).collection('approved_requests').doc(trainer_id).delete();

        return admin.messaging().sendToTopic("trainer_accepted_trainee_request" + trainee_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });
});


exports.trainee_accepted_trainer_request = functions.firestore.document('/business_users/{trainer_id}/approved_requests/{trainee_id}').onCreate((snap, context) => {
    const trainer_id = context.params.trainer_id;
    const trainee_id = context.params.trainee_id;

    console.log('Push notification event triggered');


    db.collection('regular_users').doc(trainee_id).get().then((doc) => {

        const trainee_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'Request accepted',
                body: trainee_name + ' is your new trainee!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        let deleteDoc = db.collection('business_users').doc(trainer_id).collection('approved_requests').doc(trainee_id).delete();

        return admin.messaging().sendToTopic("trainee_accepted_trainer_request" + trainer_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });
});


exports.workout_update = functions.firestore.document('/regular_users/{trainee_id}/updates/workout_update').onCreate((snap, context) => {
    const trainee_id = context.params.trainee_id;
    const trainer_id = snap.data().trainer_id;

    console.log('Push notification event triggered');


   db.collection('business_users').doc(trainer_id).get().then((doc) => {


        const trainer_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'Workout plan update',
                body: trainer_name + ' updated your workout plan!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };


        let deleteDoc = db.collection('regular_users').doc(trainee_id).collection('updates').doc('workout_update').delete();

        return admin.messaging().sendToTopic("workout_update" + trainee_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });


});


exports.nutrition_menu_update = functions.firestore.document('/regular_users/{trainee_id}/updates/nutrition_menu_update').onCreate((snap, context) => {
    const trainee_id = context.params.trainee_id;
    const trainer_id = snap.data().trainer_id;

    console.log('Push notification event triggered');


   db.collection('business_users').doc(trainer_id).get().then((doc) => {


        const trainer_name = doc.data().name;


        // Create a notification
        const payload = {
            notification: {
                title: 'Nutrition menu update',
                body: trainer_name + ' updated your nutrition menu!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        let deleteDoc = db.collection('regular_users').doc(trainee_id).collection('updates').doc('nutrition_menu_update').delete();

        return admin.messaging().sendToTopic("nutrition_menu_update" + trainee_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });


});

exports.appoitmentUpdate = functions.firestore.document('/regular_users/{traineeId}/appointments_updates/{docId}').onCreate((snap, context) => {
    const traineeId = context.params.traineeId;
    const docId = context.params.docId;
    const trainerId = snap.data().trainerId;
    const oldDate = snap.data().oldDate;
    const newDate = snap.data().newDate;

    console.log('Push notification event triggered');
    // console.log(traineeId + " " + docId + " " + trainerId + " " + oldDate + " " + newDate);


   return db.collection('business_users').doc(trainerId).get().then((doc) => {
        // if (!doc.exists) {
        //     console.log("doc not found")
        // }

        const trainer_name = doc.data().name;
        // console.log(trainer_name);

        let payload = "";
       // Create a notification
       // console.log(newDate + " " + oldDate)
        if (newDate !== "" && oldDate !== "") {
            payload = {
                notification: {
                    title: 'Appointment reschedule',
                    body: trainer_name + ' has rescheduled your appointment from ' + oldDate + ' to ' + newDate + '!',
                    sound: "default"
                },
            };
        }
        else if (newDate !== "" && oldDate === "") {
            payload = {
                notification: {
                    title: 'Appointment schedule',
                    body: trainer_name + ' has scheduled your appointment on ' + newDate + '!',
                    sound: "default"
                },
            };
        }
        else {
            payload = {
                notification: {
                    title: 'Appointment cancelation',
                    body: trainer_name + ' has canceled your appointment on ' + oldDate + '!',
                    sound: "default"
                },
            };
        }


        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        let deleteDoc = db.collection('regular_users').doc(traineeId).collection('appointments_updates').doc(docId).delete();
        console.log(payload);
        return admin.messaging().sendToTopic("trainee_reschedule" + traineeId, payload, options);

    }).catch(error => {

        console.log(error);
    });


});


//not notifications cloud functions:

//1.
exports.pending_request_home_screen1 = functions.firestore.document('/business_users/{trainer_id}/requests/{trainee_id}').onCreate((snap, context) => {
    const trainer_id = context.params.trainer_id;
    const trainee_id = context.params.trainee_id;

    return db.doc('/business_users/' + trainer_id + '/notifications/pending_requests').set({notification: 'You have new pending requests'});
});


//2.
exports.pending_request_home_screen2 = functions.firestore.document('/regular_users/{trainee_id}/requests/{trainer_id}').onCreate((snap, context) => {
    const trainer_id = context.params.trainer_id;
    const trainee_id = context.params.trainee_id;

    return db.doc('/regular_users/' + trainee_id + '/notifications/pending_requests').set({notification: 'You have new pending requests'});
});











