const joseLib = require("jose");

/*const { compactDecrypt } = require('jose/jwe/compact/decrypt')
const { compactVerify } = require('jose/jws/compact/verify')*/
const  crypto = require("crypto");
const fs = require("fs");
const jwt = require('jsonwebtoken');
const { Console } = require('console');

async function decrypt(){

    /****  INPUTS **********/
        //CLAIMS DMR VM AQE
    var client ='DMR';
    //Token is encrypted using Recipient Public Key and Signed with Sender Private Key
    //jwe token received from the upload response
    const jwe = 'xxxx';

    //CLIENT PRIVATE KEY [Change path to the private Key of appropriate client]
    const client_private_key = "keys_old/decrypted/private_dmr.pem";
    /****  END OF INPUTS*****/


    const privatekey = crypto.createPrivateKey(fs.readFileSync(client_private_key, 'utf8'))
    const receiverprivatekeyread = fs.readFileSync(client_private_key, 'utf8');
    /** decrypt **/
    const { plaintext, protectedHeader } = await joseLib.compactDecrypt(jwe, privatekey)
    const decoder = new TextDecoder()
    console.log("t "+plaintext)

    //READ THE JWT TOKEN      
    let base64Url = (decoder.decode(plaintext)).split('.')[1]; // token you get
    let base64 = base64Url.replace('-', '+').replace('_', '/');
    let decodedData = JSON.parse(Buffer.from(base64, 'base64').toString('binary'));

    console.log('PAYLOAD PLAINTEXT: ');
    console.log(decodedData)

    //GENERATE TOKEN WITH THE EXPECTED PAYLOAD FOR DOWNLOAD     
    var h  = {"alg":"RS256", "typ":"JWT"};
    if (client === "CLAIMS") {
        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-e264-4028-9881-8c8cba20eb7c"};
        //h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-94df-4d6b-908a-13ee5dba900d"};
    } else if (client === "DMR") {

        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-49d3-4463-bd28-70efba817c1e"};
    } else if (client === "VM") {

        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-fMuT8N188cHHbE"};

    }else if (client === "AQE"){

        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-DMgpbbSDKV_0KTg"};
    }else if (client === "CHAT"){

        h  = {"alg":"RS256", "typ":"JWT", "kid":""};
    }

    let currentTime = new Date().getTime();
    let updatedTIme = new Date(currentTime + 2 * 60 * 60 * 1000);

    var scannedpayload = {
        cvs_av_file_ref: decodedData.cvs_av_file_ref,
        "x-lob": "security-engineering",
        "scope": "openid email",
        jti: (Math.random() + 1).toString(36).substring(2),
        aud: 'CVS-AVScan',
        iss: 'Visit-Manager',
        sub: 'download_bearer_token'
    };
    signOptions = {
        algorithm:   "RS256",            // RSASSA options[ "RS256", "RS384", "RS512" ]      
        header: h,
        expiresIn: '3600s'
    };

    console.log("");
    console.log("INPUTS FOR THE DOWNLOAD CALL:");
    console.log("");
    console.log("FILE NAME:")
    console.log(decodedData.cvs_av_file_ref);
    console.log("")
    console.log("BEARER TOKEN FOR DOWNLOAD:")
    console.log(jwt.sign(scannedpayload, receiverprivatekeyread,signOptions));

}

decrypt();

 joseLib = require("jose");/*const { compactDecrypt } = require('jose/jwe/compact/decrypt')const { compactVerify } = require('jose/jws/compact/verify')*/const  crypto = require("crypto");
 const fs = require("fs");
 const jwt = require('jsonwebtoken');
 const { Console } = require('console');
 async function decrypt(){   
 /****  INPUTS **********/        //CLAIMS DMR VM AQE    var client ='DMR';
 //Token is encrypted using Recipient Public Key and Signed with Sender Private Key    //jwe token received from the upload response
 const jwe = 'cccc';    //CLIENT PRIVATE KEY [Change path to the private Key of appropriate client]
 const client_private_key = "keys_old/decrypted/private_dmr.pem";
 /****  END OF INPUTS*****/ 
 const privatekey = crypto.createPrivateKey(fs.readFileSync(client_private_key, 'utf8')) 
 const receiverprivatekeyread = fs.readFileSync(client_private_key, 'utf8'); 
 /** decrypt **/    const { plaintext, protectedHeader } = await joseLib.compactDecrypt(jwe, privatekey)    const decoder = new TextDecoder()
 console.log("t "+plaintext)    //READ THE JWT TOKEN   
 let base64Url = (decoder.decode(plaintext)).split('.')[1]; // token you get    let base64 = base64Url.replace('-', '+').replace('_', '/'); 
 let decodedData = JSON.parse(Buffer.from(base64, 'base64').toString('binary'));    console.log('PAYLOAD PLAINTEXT: ');  
 console.log(decodedData)    //GENERATE TOKEN WITH THE EXPECTED PAYLOAD FOR DOWNLOAD    
 var h  = {"alg":"RS256", "typ":"JWT"};    if (client === "CLAIMS") {        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-e264-4028-9881-8c8cba20eb7c"};   
 //h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-94df-4d6b-908a-13ee5dba900d"};    } else if (client === "DMR") {   
 h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-49d3-4463-bd28-70efba817c1e"};    } 
 else if (client === "VM") {        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-fMuT8N188cHHbE"};
 }else if (client === "AQE"){        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-DMgpbbSDKV_0KTg"}; 
 }else if (client === "CHAT"){        h  = {"alg":"RS256", "typ":"JWT", "kid":""};    } 
 let currentTime = new Date().getTime();    let updatedTIme = new Date(currentTime + 2 * 60 * 60 * 1000);    
 var scannedpayload = {        cvs_av_file_ref: decodedData.cvs_av_file_ref,        
 "x-lob": "security-engineering",        "scope": "openid email",      
 jti: (Math.random() + 1).toString(36).substring(2),        aud: 'CVS-AVScan',        iss: 'Visit-Manager',        sub: 'download_bearer_token'    }; 
 signOptions = {        algorithm:   "RS256",            // RSASSA options[ "RS256", "RS384", "RS512" ]              header: h,        expiresIn: '3600s'    }; 
 console.log("");  
 console.log("INPUTS FOR THE DOWNLOAD CALL:");    console.log("");    console.log("FILE NAME:")  
 console.log(decodedData.cvs_av_file_ref);  
 console.log("")   
 console.log("BEARER TOKEN FOR DOWNLOAD:")
 console.log(jwt.sign(scannedpayload, receiverprivatekeyread,signOptions));}decrypt(); 
