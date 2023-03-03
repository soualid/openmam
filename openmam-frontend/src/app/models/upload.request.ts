
export interface UploadRequest {
    id: number;
    status: UploadRequestStatus;
    partner: any;
    presignedUploadURL: string;
}


export enum UploadRequestStatus {
  PENDING = 'PENDING',
  UPLOADED = 'UPLOADED',
  INGESTING = "INGESTING"
}
