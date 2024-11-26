To call the above cURL request using Angular 16, you can use Angular’s HttpClient module. Here’s how you can do it:

Steps:

	1.	Import the HttpClientModule in your Angular application’s AppModule.
	2.	Use Angular’s HttpClient service to make the POST request.

Example Code:

1. AppModule Setup

Make sure HttpClientModule is imported in your app.module.ts:

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

2. Service to Handle the API Request

Create a service to manage the API call:

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FileScanService {
  private apiUrl = 'https://sit1-api.cvshealth.com/file/scan/v1/upload';

  constructor(private http: HttpClient) {}

  uploadFile(fileContent: string, state: string, fileName: string): Observable<any> {
    const headers = new HttpHeaders({
      'x-api-key': 'T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w',
      'Content-Type': 'application/json',
    });

    const body = {
      file: fileContent,
      state: state,
      fileName: fileName,
    };

    return this.http.post(this.apiUrl, body, { headers });
  }
}

3. Calling the Service in a Component

Use the service in a component to make the API request:

import { Component } from '@angular/core';
import { FileScanService } from './file-scan.service';

@Component({
  selector: 'app-root',
  template: `
    <button (click)="sendRequest()">Upload File</button>
    <div *ngIf="response">
      <h3>Response:</h3>
      <pre>{{ response | json }}</pre>
    </div>
  `,
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  response: any;

  constructor(private fileScanService: FileScanService) {}

  sendRequest(): void {
    const fileContent = 'JVBERi0xLjMKJbrfrOAKMyAwIG9iago8PC9';
    const state = 'teststate';
    const fileName = 'screenshot';

    this.fileScanService.uploadFile(fileContent, state, fileName).subscribe(
      (res) => {
        this.response = res;
      },
      (err) => {
        console.error('Error occurred:', err);
      }
    );
  }
}

Explanation:

	1.	HttpHeaders: Used to set the x-api-key and Content-Type headers.
	2.	HttpClient.post: Sends a POST request to the API with the required headers and body.
	3.	Observable: Subscribes to the API response to handle success or error cases.

Running the Code

	•	Click the “Upload File” button in your Angular application.
	•	View the response in the component template or console.

This setup should work seamlessly with Angular 16. Let me know if you face any issues!
