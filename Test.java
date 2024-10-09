To send a request to the Spring Boot file upload endpoint from an Angular 17 application, you can use Angular's `HttpClient` service to handle multipart form data. Here’s an example of how you can build a service and send the request.

### Angular Service (FileUploadService)

```typescript
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FileUploadService {
  private uploadUrl = 'http://your-api-url/file'; // Replace with your API URL

  constructor(private http: HttpClient) {}

  uploadFile(file: File, formData: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file); // Append the file
    uploadData.append('quoteId', formData.quoteId); 
    uploadData.append('docTyp', formData.docTyp); 
    uploadData.append('docCategory', formData.docCategory); 
    uploadData.append('docSubcategory', formData.docSubcategory); 
    uploadData.append('docSize', formData.docSize);
    uploadData.append('docQuoteStage', formData.docQuoteStage); 
    uploadData.append('uploadedUsrId', formData.uploadedUsrId); 
    uploadData.append('uploadedUsrNm', formData.uploadedUsrNm);

    // Set headers if necessary
    const headers = new HttpHeaders({
      'Accept': 'application/json',
    });

    return this.http.post<any>(this.uploadUrl, uploadData, { headers });
  }
}
```

### Angular Component (FileUploadComponent)

In your component, you can create a form, bind it to the `FileUploadService`, and send the file and metadata.

```typescript
import { Component } from '@angular/core';
import { FileUploadService } from './file-upload.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent {
  uploadForm: FormGroup;

  constructor(private fileUploadService: FileUploadService, private fb: FormBuilder) {
    this.uploadForm = this.fb.group({
      file: [null, Validators.required],
      quoteId: ['', Validators.required],
      docTyp: ['SG', Validators.required],
      docCategory: ['IMQ', Validators.required],
      docSubcategory: ['SBC', Validators.required],
      docSize: ['1024', Validators.required],
      docQuoteStage: ['Initial Submission', Validators.required],
      uploadedUsrId: ['N993527', Validators.required],
      uploadedUsrNm: ['FirstName LastName', Validators.required],
    });
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    this.uploadForm.patchValue({
      file: file,
    });
  }

  upload() {
    if (this.uploadForm.valid) {
      const formData = this.uploadForm.value;
      this.fileUploadService.uploadFile(formData.file, formData).subscribe(
        (response) => {
          console.log('File uploaded successfully', response);
        },
        (error) => {
          console.error('Error uploading file', error);
        }
      );
    }
  }
}
```

### HTML Template (file-upload.component.html)

```html
<form [formGroup]="uploadForm" (ngSubmit)="upload()">
  <input type="file" (change)="onFileChange($event)" required />
  
  <input formControlName="quoteId" placeholder="Quote ID" required />
  <input formControlName="docTyp" placeholder="Document Type" required />
  <input formControlName="docCategory" placeholder="Document Category" required />
  <input formControlName="docSubcategory" placeholder="Document Subcategory" required />
  <input formControlName="docSize" placeholder="Document Size" required />
  <input formControlName="docQuoteStage" placeholder="Document Quote Stage" required />
  <input formControlName="uploadedUsrId" placeholder="Uploaded User ID" required />
  <input formControlName="uploadedUsrNm" placeholder="Uploaded User Name" required />

  <button type="submit">Upload</button>
</form>
```

### Steps:
1. The user selects a file via the file input.
2. The file and metadata (quoteId, docTyp, etc.) are sent to the backend via the `FileUploadService`.
3. You can handle the success or error response accordingly in the Angular component.

Let me know if you'd like further details on integrating this into your project!
