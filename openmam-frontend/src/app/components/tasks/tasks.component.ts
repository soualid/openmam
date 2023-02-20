import { Component } from '@angular/core';
import { Page } from 'src/app/models/page';
import { Task } from 'src/app/models/task';
import { TaskService } from 'src/app/services/task.service';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.sass']
})
export class TasksComponent {

  result: Page<Task> = {}
  loading: boolean = true
  dataSource: Task[] = []
  columnsToDisplay: string[] = ['id', 'type', 'creationDate', 'endDate', 'status', 'lockedBy']

  constructor(private taskService: TaskService) { }

  ngOnInit(): void {
    this.getTasks();
  }
  
  getTasks(): void {
    this.taskService.getTasks()
      .subscribe(result => {
        this.result = result
        this.loading = false
        this.dataSource = this.result.content ?? []
        console.log('set datasource to', this.dataSource)
      });
  }

}
