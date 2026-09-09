export type DetectorMode = 'RT' | 'RT-M' | 'RT-M99' | 'RTL-ST' | 'RT-ST' | 'ST-P';

export interface SignumProfile {
  program: 'P1' | 'P2' | 'P3' | 'P4' | 'DP';
  mode: DetectorMode;
  sensitivity: number;
  recoveryTime: number;
  gain: number;
  discriminationBorder: number;
  mmSpeed: number;
  groundScale: number;
  filterT: number;
  filterA: number;
}

export const DEFAULT_PROFILE: SignumProfile = {
  program: 'P3',
  mode: 'RT-ST',
  sensitivity: 11,
  recoveryTime: 8,
  gain: 7,
  discriminationBorder: -24,
  mmSpeed: 3,
  groundScale: 0,
  filterT: 15,
  filterA: 2,
};
