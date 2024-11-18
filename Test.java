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
    const jwe = 'eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.SRPwn7LF4BSLwvsgpc4fx8y-BLqIfwi2qmVn2IK7T5ZuX454TGfXDDyQ_tJ6eDhlc9kZORMNef9rgd6MW_kzuF6Vjb-7ID9lFrC6qizIwRN_fy4l-NVP9bGfafgjU97Z2ywbRM7OOQMrhfgLRzOix2FAnyL1CgCikJJnnwr-iYulGO23fMOYyzVG--9QGSF5wQ2d8iOMMhQdC6lstWGVHDAkRy6i42iaiOAJSn9awE3f0iqAO1zSAd6dhAjX2eAWxMPRj0Op3T1iPqo69YqSjAtGOR7GtZm5aXj9PcfngEGJaUnihyJ1Kj-qmpEsbJHcAnL8LSOjGkX83vO-jABaydT64jHZTInIEOGwHCtRUIjUkkTTObInhQUnCnBVRhwjiuBkIoJCbZs2yF4YDsle4nkAuEObyCdumTsxWE4AnZLQv1tPwrSvVDOCoWoWuDcdf1TFk24j02z2XeMh1H2HaFV0ddHMxUrwgi-O6xGViT8lcEpVOjOPRzckQpsOBLZF7Oo_YMKGiAcjULJzCUPWQKvAco70KHFDFUx4k-XUmbFM27zw1dKtootDqW_7Mbg2tl7EHjuLwJHZwjng9YYmo2k8nugCFAKuin7wZIM-CqIbXA2Vm1jmwwypvnC4dc4L_H1aQneP4Fv6EI2MlSEdxCJYK_VNPr-IQaJx-PZyq5o.O3YZcUSrj9Hr-JKk.rqS9e6wQTuINZkDqvq6C4Q_RWJiIqN2nLP-6O8DnJpCVsGOQCPP99vx3NRBGzbJS0Mt18sYrzAQrzp7d54i-gYcWMsx152RRoZwfm9yfyrfoZsPPKqHvK3hW1ySeCWYOqZD77TJEa8OIed1NiukfLlJa_V-4bkPmm1vSk2QwSCLgGQU32oqKTrTQ6-ebO51ohccwnIrmdGOzw-bI7TkT82wIrfmCnV-zXC2AmZGZYf0Vk9BkcWG7FRUNa7aTYqNk1cXks5_nVFn_lNCs1gqwpJ3f2An0YwUiEhByHosrAixPQnZySovWNYZgB6DHzoL3ZwAtpS5xFoavQQ3sgcrJj5v7J6fgqMO7KoX4rAWd4tr0SsVk7hJRUG7eXF_03Z5RKOi1N7oyzl7VOYm_N5Lv1iur2i_9j217g3bOv9XFIMywK3aEduqxRTcZ8A_IDikxTibnBlnJtPa568YrIxpLc0b6sIdjd4301hU_DEU3wWPoIjiztm7S8EGVZQc-Gd0mXHexdBzO_YkE18ATloWFGP7X7RuKvlTcVY3yRsHKiwGDTj1VvSZN6YpgPYP_8XSrIkHDaNdNuVcUQAK0FqXi-eyGfsLKTqK5XG3MJ7fBzvjafU9-gz2DBmlWLAdslbTQ-lKZEyxaj7iqJjMgzpJf3X3Z1h28sX2TvlzJdCcpG_WAzWpvk4Tg36q3dPpDwHBN4EWmQd4sCwm-a0SSub0kkUdWCVz8rRIvGMD3NjQs0c3bKeXi-GbsxuOa7aBIPz9OTFdcMDtOL37hEzS827NfrnLwBQMa6ZV91ZOsskm5VOwn20zD6aq4CFnfGnN3jbbec6zr3m-UeV7YwKPshT2N8DPBHZhID4jr0S4kazga6dOB8mLCVW4va4SXmA.1J8vV9-TxRUSdkjO5_43Ag';

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

 joseLib = require("jose");/*const { compactDecrypt } = require('jose/jwe/compact/decrypt')const { compactVerify } = require('jose/jws/compact/verify')*/const  crypto = require("crypto");const fs = require("fs");const jwt = require('jsonwebtoken');const { Console } = require('console');async function decrypt(){    /****  INPUTS **********/        //CLAIMS DMR VM AQE    var client ='DMR';    //Token is encrypted using Recipient Public Key and Signed with Sender Private Key    //jwe token received from the upload response    const jwe = 'eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.SRPwn7LF4BSLwvsgpc4fx8y-BLqIfwi2qmVn2IK7T5ZuX454TGfXDDyQ_tJ6eDhlc9kZORMNef9rgd6MW_kzuF6Vjb-7ID9lFrC6qizIwRN_fy4l-NVP9bGfafgjU97Z2ywbRM7OOQMrhfgLRzOix2FAnyL1CgCikJJnnwr-iYulGO23fMOYyzVG--9QGSF5wQ2d8iOMMhQdC6lstWGVHDAkRy6i42iaiOAJSn9awE3f0iqAO1zSAd6dhAjX2eAWxMPRj0Op3T1iPqo69YqSjAtGOR7GtZm5aXj9PcfngEGJaUnihyJ1Kj-qmpEsbJHcAnL8LSOjGkX83vO-jABaydT64jHZTInIEOGwHCtRUIjUkkTTObInhQUnCnBVRhwjiuBkIoJCbZs2yF4YDsle4nkAuEObyCdumTsxWE4AnZLQv1tPwrSvVDOCoWoWuDcdf1TFk24j02z2XeMh1H2HaFV0ddHMxUrwgi-O6xGViT8lcEpVOjOPRzckQpsOBLZF7Oo_YMKGiAcjULJzCUPWQKvAco70KHFDFUx4k-XUmbFM27zw1dKtootDqW_7Mbg2tl7EHjuLwJHZwjng9YYmo2k8nugCFAKuin7wZIM-CqIbXA2Vm1jmwwypvnC4dc4L_H1aQneP4Fv6EI2MlSEdxCJYK_VNPr-IQaJx-PZyq5o.O3YZcUSrj9Hr-JKk.rqS9e6wQTuINZkDqvq6C4Q_RWJiIqN2nLP-6O8DnJpCVsGOQCPP99vx3NRBGzbJS0Mt18sYrzAQrzp7d54i-gYcWMsx152RRoZwfm9yfyrfoZsPPKqHvK3hW1ySeCWYOqZD77TJEa8OIed1NiukfLlJa_V-4bkPmm1vSk2QwSCLgGQU32oqKTrTQ6-ebO51ohccwnIrmdGOzw-bI7TkT82wIrfmCnV-zXC2AmZGZYf0Vk9BkcWG7FRUNa7aTYqNk1cXks5_nVFn_lNCs1gqwpJ3f2An0YwUiEhByHosrAixPQnZySovWNYZgB6DHzoL3ZwAtpS5xFoavQQ3sgcrJj5v7J6fgqMO7KoX4rAWd4tr0SsVk7hJRUG7eXF_03Z5RKOi1N7oyzl7VOYm_N5Lv1iur2i_9j217g3bOv9XFIMywK3aEduqxRTcZ8A_IDikxTibnBlnJtPa568YrIxpLc0b6sIdjd4301hU_DEU3wWPoIjiztm7S8EGVZQc-Gd0mXHexdBzO_YkE18ATloWFGP7X7RuKvlTcVY3yRsHKiwGDTj1VvSZN6YpgPYP_8XSrIkHDaNdNuVcUQAK0FqXi-eyGfsLKTqK5XG3MJ7fBzvjafU9-gz2DBmlWLAdslbTQ-lKZEyxaj7iqJjMgzpJf3X3Z1h28sX2TvlzJdCcpG_WAzWpvk4Tg36q3dPpDwHBN4EWmQd4sCwm-a0SSub0kkUdWCVz8rRIvGMD3NjQs0c3bKeXi-GbsxuOa7aBIPz9OTFdcMDtOL37hEzS827NfrnLwBQMa6ZV91ZOsskm5VOwn20zD6aq4CFnfGnN3jbbec6zr3m-UeV7YwKPshT2N8DPBHZhID4jr0S4kazga6dOB8mLCVW4va4SXmA.1J8vV9-TxRUSdkjO5_43Ag';    //CLIENT PRIVATE KEY [Change path to the private Key of appropriate client]    const client_private_key = "keys_old/decrypted/private_dmr.pem";    /****  END OF INPUTS*****/    const privatekey = crypto.createPrivateKey(fs.readFileSync(client_private_key, 'utf8'))    const receiverprivatekeyread = fs.readFileSync(client_private_key, 'utf8');    /** decrypt **/    const { plaintext, protectedHeader } = await joseLib.compactDecrypt(jwe, privatekey)    const decoder = new TextDecoder()    console.log("t "+plaintext)    //READ THE JWT TOKEN          let base64Url = (decoder.decode(plaintext)).split('.')[1]; // token you get    let base64 = base64Url.replace('-', '+').replace('_', '/');    let decodedData = JSON.parse(Buffer.from(base64, 'base64').toString('binary'));    console.log('PAYLOAD PLAINTEXT: ');    console.log(decodedData)    //GENERATE TOKEN WITH THE EXPECTED PAYLOAD FOR DOWNLOAD         var h  = {"alg":"RS256", "typ":"JWT"};    if (client === "CLAIMS") {        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-e264-4028-9881-8c8cba20eb7c"};        //h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-94df-4d6b-908a-13ee5dba900d"};    } else if (client === "DMR") {        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-49d3-4463-bd28-70efba817c1e"};    } else if (client === "VM") {        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-fMuT8N188cHHbE"};    }else if (client === "AQE"){        h  = {"alg":"RS256", "typ":"JWT", "kid":"abc-DMgpbbSDKV_0KTg"};    }else if (client === "CHAT"){        h  = {"alg":"RS256", "typ":"JWT", "kid":""};    }    let currentTime = new Date().getTime();    let updatedTIme = new Date(currentTime + 2 * 60 * 60 * 1000);    var scannedpayload = {        cvs_av_file_ref: decodedData.cvs_av_file_ref,        "x-lob": "security-engineering",        "scope": "openid email",        jti: (Math.random() + 1).toString(36).substring(2),        aud: 'CVS-AVScan',        iss: 'Visit-Manager',        sub: 'download_bearer_token'    };    signOptions = {        algorithm:   "RS256",            // RSASSA options[ "RS256", "RS384", "RS512" ]              header: h,        expiresIn: '3600s'    };    console.log("");    console.log("INPUTS FOR THE DOWNLOAD CALL:");    console.log("");    console.log("FILE NAME:")    console.log(decodedData.cvs_av_file_ref);    console.log("")    console.log("BEARER TOKEN FOR DOWNLOAD:")    console.log(jwt.sign(scannedpayload, receiverprivatekeyread,signOptions));}decrypt(); 
