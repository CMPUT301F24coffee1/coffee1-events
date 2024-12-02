export interface AppEvent {
  lotteryProcessed: boolean;
  startDate: number;
  endDate: number;
  deadline: number;
  maxEntrants: number;
  numberOfAttendees: number;
  eventName: string;
  organizerId: string;
}
