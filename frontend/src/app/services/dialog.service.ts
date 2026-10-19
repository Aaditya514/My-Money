import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export type DialogType = 'danger' | 'warning' | 'info' | 'primary';
export type DialogIcon = 'trash' | 'alert' | 'info' | 'logout' | 'key' | 'question' | 'success';

export interface ConfirmDialogOptions {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: DialogType;
  icon?: DialogIcon;
}

export interface AlertDialogOptions {
  title?: string;
  message: string;
  buttonText?: string;
  confirmText?: string;
  okText?: string;
  type?: DialogType;
  icon?: DialogIcon;
}

export interface DialogState {
  isOpen: boolean;
  isAlertOnly: boolean;
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  type: DialogType;
  icon: DialogIcon;
  resolve?: (result: boolean) => void;
}

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private dialogStateSubject = new Subject<DialogState>();
  public dialogState$: Observable<DialogState> = this.dialogStateSubject.asObservable();

  /**
   * Show a modern confirmation modal that replaces native confirm()
   */
  confirm(options: ConfirmDialogOptions | string): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      let opts: ConfirmDialogOptions;
      if (typeof options === 'string') {
        opts = {
          message: options,
          title: 'Confirm Action',
          confirmText: 'Confirm',
          cancelText: 'Cancel',
          type: 'primary',
          icon: 'question'
        };
      } else {
        opts = options;
      }

      this.dialogStateSubject.next({
        isOpen: true,
        isAlertOnly: false,
        title: opts.title || 'Confirm Action',
        message: opts.message,
        confirmText: opts.confirmText || 'Confirm',
        cancelText: opts.cancelText || 'Cancel',
        type: opts.type || (opts.icon === 'trash' ? 'danger' : 'primary'),
        icon: opts.icon || (opts.type === 'danger' ? 'trash' : 'question'),
        resolve
      });
    });
  }

  /**
   * Convenience shortcut for destructive actions (e.g. Delete)
   */
  danger(message: string, title: string = 'Confirm Deletion', confirmText: string = 'Delete'): Promise<boolean> {
    return this.confirm({
      title,
      message,
      confirmText,
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'trash'
    });
  }

  /**
   * Show a modern alert dialog that replaces native alert()
   */
  alert(options: AlertDialogOptions | string): Promise<void> {
    return new Promise<void>((resolve) => {
      let opts: AlertDialogOptions;
      if (typeof options === 'string') {
        opts = {
          message: options,
          title: 'Notification',
          buttonText: 'Got It',
          type: 'info',
          icon: 'info'
        };
      } else {
        opts = options;
      }

      this.dialogStateSubject.next({
        isOpen: true,
        isAlertOnly: true,
        title: opts.title || 'Notification',
        message: opts.message,
        confirmText: opts.buttonText || opts.confirmText || opts.okText || 'Got It',
        cancelText: '',
        type: opts.type || 'info',
        icon: opts.icon || 'info',
        resolve: () => resolve()
      });
    });
  }
}
