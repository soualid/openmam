
export interface Task {
    id?: number;
    type: TaskType;
    createdBy: string;
    lockedBy: string;
    status: TaskStatus;
}

export enum TaskType {
    GENERATE_VARIANTS = 'GENERATE_VARIANTS'
}
export enum TaskStatus {
    WORKING = 'WORKING',
    PENDING = 'PENDING',
    ERROR = 'ERROR',
    SUCCEEDED = 'SUCCEEDED',
}