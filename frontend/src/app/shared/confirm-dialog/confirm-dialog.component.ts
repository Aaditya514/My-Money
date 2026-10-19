import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogService, DialogState } from '../../services/dialog.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent implements OnInit, OnDestroy {
  state: DialogState = {
    isOpen: false,
    isAlertOnly: false,
    title: '',
    message: '',
    confirmText: 'Confirm',
    cancelText: 'Cancel',
    type: 'primary',
    icon: 'question'
  };

  private sub?: Subscription;

  constructor(private dialogService: DialogService) {}

  ngOnInit(): void {
    this.sub = this.dialogService.dialogState$.subscribe((newState) => {
      this.state = newState;
      if (newState.isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    document.body.style.overflow = '';
  }

  @HostListener('window:keydown.escape')
  onEscape(): void {
    if (this.state.isOpen) {
      this.onCancel();
    }
  }

  onConfirm(): void {
    if (this.state.resolve) {
      this.state.resolve(true);
    }
    this.close();
  }

  onCancel(): void {
    if (this.state.resolve) {
      this.state.resolve(false);
    }
    this.close();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop')) {
      this.onCancel();
    }
  }

  private close(): void {
    this.state.isOpen = false;
    document.body.style.overflow = '';
  }
}
